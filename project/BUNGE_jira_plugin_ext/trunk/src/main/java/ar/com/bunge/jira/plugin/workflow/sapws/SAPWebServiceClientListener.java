/*
 * File name: SAPWebServiceClientListener.java
 * Creation date: Aug 14, 2009 4:10:26 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.sapws;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import ar.com.bunge.jira.plugin.workflow.utils.IssueUtils;
import ar.com.bunge.jira.plugin.workflow.utils.LogUtils;
import ar.com.bunge.jira.plugin.workflow.utils.WorkflowUtils;
import ar.com.bunge.sapws.client.SAPClientXmlResponse;
import ar.com.bunge.sapws.client.SAPWSClient;
import ar.com.bunge.util.Utils;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("unchecked")
public class SAPWebServiceClientListener extends AbstractIssueEventListener implements IssueEventListener {
	private static final Logger LOG = Logger.getLogger(SAPWebServiceClientListener.class);
	
	private static final String URL_PARAM = "URL";
	private static final String USERNAME_PARAM = "Username";
	private static final String PASSWORD_PARAM = "Password";
	private static final String REQUEST_TEMPLATE_PATH_PARAM = "Path to Request Template";
	private static final String RESPONSE_STATUS_PARAM = "Response Status Field Name";
	private static final String RESPONSE_MESSAGE_PARAM = "Response Message Field Name";
	private static final String RESPONSE_XML_PARAM = "Response XML Field Name";
	private static final String ENABLED_EVENTS = "Enabled Events";
	
	private String statusFieldName = null;
	private String messageFieldName = null;
	private String responseFieldName = null;
	private List<String> events = new ArrayList<String>();
	private Map<Long, String> availableEvents = null;
	
	private final SAPWSClient client = new SAPWSClient();
	
	/**
	 * 
	 */
	public SAPWebServiceClientListener() {
	}

	/**
	 * @return
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#getAcceptedParams()
	 */
	public String[] getAcceptedParams() {
		return new String[] {URL_PARAM, USERNAME_PARAM, PASSWORD_PARAM, REQUEST_TEMPLATE_PATH_PARAM, RESPONSE_STATUS_PARAM, RESPONSE_MESSAGE_PARAM, RESPONSE_XML_PARAM, ENABLED_EVENTS};
	}

	/**
	 * @return
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#getDescription()
	 */
	public String getDescription() {
		return "Invokes a Web Service published on URL " + getClient().getUrl() + " for events " + getEvents();
	}

	/**
	 * @param params
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#init(java.util.Map)
	 */
	public void init(Map params) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpMap(SAPWebServiceClientListener.class.getName() + ".init", params));
		}
		
		if(params != null) {
			getClient().setBasicAuthentication(true);
			getClient().setUrl((String) params.get(URL_PARAM));
			getClient().setUsername((String) params.get(USERNAME_PARAM));
			getClient().setPassword((String) params.get(PASSWORD_PARAM));
			getClient().setRequestTemplateFile((String) params.get(REQUEST_TEMPLATE_PATH_PARAM));
			
			setStatusFieldName((String) params.get(RESPONSE_STATUS_PARAM));
			setMessageFieldName((String) params.get(RESPONSE_MESSAGE_PARAM));
			setResponseFieldName((String) params.get(RESPONSE_XML_PARAM));
			parseEnabledEvents((String) params.get(ENABLED_EVENTS));
			
			if(LOG.isDebugEnabled()) {
				LOG.debug(getClient().toString());
				LOG.debug("Writing response status to issue field [" + getStatusFieldName() + "]");
				LOG.debug("Writing response message to issue field [" + getMessageFieldName() + "]");
				LOG.debug("Writing response XML to issue field [" + getResponseFieldName() + "]");
				LOG.debug("Enabled Events " + getEvents());
			}

		} else {
			LOG.warn("Null params map");
		}
	}

	/**
	 * 
	 * @param events
	 * @return
	 */
	private void parseEnabledEvents(String events) {
		if(events != null && events.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer(events, ",;:");
			String anEvent;
			while(st.hasMoreTokens()) {
				anEvent = st.nextToken();
				if(anEvent != null) {
					if(LOG.isDebugEnabled()) {
						LOG.debug("Adding [" + anEvent + "] to enabled events list");
					}
					getEvents().add(anEvent.trim().toLowerCase());
				}
			}
		}
	}
	
	/**
	 * 
	 * @param eventName
	 * @return
	 */
	private boolean processEvent(String eventName) {
		if(eventName != null && eventName.trim().length() > 0 ) {
			boolean process = getEvents().contains(eventName.trim().toLowerCase());
			if(LOG.isDebugEnabled()) {
				LOG.debug("Event name [" + eventName + "] is " + (process ? "" : "not ") + "present in enabled events list " + getEvents());
			}
			return process;
		} else {
			if(LOG.isDebugEnabled()) {
				LOG.debug("Event name is null or empty. Not processing event");
			}
			return false;
		}
	}
	
	/**
	 * @return the client
	 */
	protected SAPWSClient getClient() {
		return client;
	}

	/**
	 * 
	 * @param event
	 * @param eventName
	 */
	private void handleEvent(IssueEvent event) {
		SAPClientXmlResponse response = null;

		if(LOG.isDebugEnabled()) {
			LOG.debug("About to search event name for event type id [" + event.getEventTypeId() + "] in available events " + getAvailableEvents());
		}
		
		String eventName = getAvailableEvents().get(event.getEventTypeId());

		if(LOG.isDebugEnabled()) {
			LOG.debug("Found event name [" + eventName + "] for event type id [" + event.getEventTypeId() + "]");
		}
		
		if(processEvent(eventName)) {
			IssueChangeHolder holder = new DefaultIssueChangeHolder();
			
			try {
				Map<String, Object> context = IssueUtils.buildContext(event.getIssue());
				
				if(LOG.isDebugEnabled()) {
					LOG.debug(LogUtils.dumpMap("context", context));
				}

				response = getClient().execute(context);

				if(LOG.isDebugEnabled()) {
					LOG.debug(response);
				}
				
				setResponseStatusAndMessage(event.getIssue(), response.getNumberAsString(), response.getMessage(), holder);
				setFieldValue(event.getIssue(), getResponseFieldName(), response.getResponse(), holder);
			} catch(Throwable ex) {
				LOG.error("Cannot handle event [" + event.getEventTypeId() + "] -> " + ex.getLocalizedMessage(), ex);
				setResponseStatusAndMessage(event.getIssue(), "-2", ex.getLocalizedMessage(), holder);
			}			
		} else {
			if(LOG.isDebugEnabled()) {
				LOG.debug("Event [" + eventName + "] is not configured to be handled");
			}
		}			
	}
	
	/**
	 * 
	 * @param issue
	 * @param status
	 * @param message
	 * @param changeHolder
	 */
	private void setResponseStatusAndMessage(Issue issue, String status, String message, IssueChangeHolder changeHolder) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Trying to set status field [" + getStatusFieldName() + "] with value [" + status + "]");
		}
		setFieldValue(issue, getStatusFieldName(), Utils.truncate(status, 255), changeHolder);
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Trying to set message field [" + getMessageFieldName() + "] with value [" + message + "]");
		}
		setFieldValue(issue, getMessageFieldName(), Utils.truncate(message, 255), changeHolder);
	}
	
	/**
	 * 
	 * @param issue
	 * @param fieldName
	 * @param fieldValue
	 * @param changeHolder
	 */
	private void setFieldValue(Issue issue, String fieldName, String fieldValue, IssueChangeHolder changeHolder) {
		if(fieldName != null && issue instanceof MutableIssue) {
	        try {
				WorkflowUtils.setFieldValue((MutableIssue) issue, getFieldKeyFromName(fieldName), fieldValue, changeHolder);
				issue.store();
	        } catch(Throwable ex) {
	        	 LOG.error("Cannot set value [" + fieldValue + "] to field name [" + fieldName + "]: " + ex, ex);
	        }
		} else {
			if(fieldName != null) {
				LOG.info("Cannot set custom field value for a null field name.");
			} else {
				LOG.warn("Issue is not mutable");
			}
		}			
	}	
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	private String getFieldKeyFromName(String name) {

		if(name != null) {
			try {
				FieldManager fieldManager = ManagerFactory.getFieldManager();
				
				for (Iterator it = fieldManager.getAllAvailableNavigableFields().iterator(); it.hasNext(); ) {
					Field f = (Field) it.next();
					if(name.equals(f.getName())) {
						LOG.debug("Found field for name [" + name + "] with key [" + f.getId() + "]");
						return f.getId();
					}
				}
			} catch(Throwable ex) {
				LOG.error("Cannot search field key for field name [" + name + "] -> " + ex.getLocalizedMessage(), ex);
			}
		}
		
		LOG.debug("No field found with name [" + name + "]");
		
		return null;
	}		
	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#customEvent(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void customEvent(IssueEvent event) {
		handleEvent(event);
	}

	/**
	 * @return
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#isInternal()
	 */
	public boolean isInternal() {
		return false;
	}

	/**
	 * @return
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#isUnique()
	 */
	public boolean isUnique() {
		return false;
	}
	
	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueAssigned(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueAssigned(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueAssigned", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueClosed(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueClosed(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueClosed", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueCommented(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueCommented(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueCommented", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueCommentEdited(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueCommentEdited(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueCommentEdited", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueCreated(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueCreated(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueCreated", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueDeleted(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueDeleted(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueDeleted", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueGenericEvent(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueGenericEvent(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueGenericEvent", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueMoved(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueMoved(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueMoved", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueReopened(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueReopened(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueReopened", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueResolved(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueResolved(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueResolved", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueStarted(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueStarted(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueStarted", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueStopped(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueStopped(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueStopped", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueUpdated(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueUpdated(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueUpdated", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueWorklogDeleted(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueWorklogDeleted(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueWorklogDeleted", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueWorkLogged(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueWorkLogged(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueWorkLogged", event));
		}
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#issueWorklogUpdated(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void issueWorklogUpdated(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("issueWorklogUpdated", event));
		}		
		handleEvent(event);
	}

	/**
	 * @param event
	 * @see com.atlassian.jira.event.issue.AbstractIssueEventListener#workflowEvent(com.atlassian.jira.event.issue.IssueEvent)
	 */
	public void workflowEvent(IssueEvent event) {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpEvent("workflowEvent", event));
		}
		handleEvent(event);
	}

	/**
	 * @return the statusFieldName
	 */
	protected String getStatusFieldName() {
		return statusFieldName;
	}

	/**
	 * @param statusFieldName the statusFieldName to set
	 */
	protected void setStatusFieldName(String statusFieldName) {
		this.statusFieldName = statusFieldName;
	}

	/**
	 * @return the messageFieldName
	 */
	protected String getMessageFieldName() {
		return messageFieldName;
	}

	/**
	 * @param messageFieldName the messageFieldName to set
	 */
	protected void setMessageFieldName(String messageFieldName) {
		this.messageFieldName = messageFieldName;
	}

	/**
	 * @return the responseFieldName
	 */
	protected String getResponseFieldName() {
		return responseFieldName;
	}

	/**
	 * @param responseFieldName the responseFieldName to set
	 */
	protected void setResponseFieldName(String responseFieldName) {
		this.responseFieldName = responseFieldName;
	}

	/**
	 * @return the events
	 */
	protected List<String> getEvents() {
		return events;
	}

	/**
	 * @param events the events to set
	 */
	protected void setEvents(List<String> events) {
		this.events = events;
	}

	/**
	 * @return the availableEvents
	 */
	protected Map<Long, String> getAvailableEvents() {
		if(availableEvents == null) {
			Collection<Long> eventTypes = ComponentManager.getInstance().getEventTypeManager().getEventTypes();
			if(eventTypes != null) {
				if(LOG.isDebugEnabled()) {
					LOG.debug("Found available event types [" + eventTypes + "]");
				}
				setAvailableEvents(new HashMap<Long, String>(eventTypes.size()));
				String eventName;
				Long eventTypeId;
				for(Iterator<Long> it = eventTypes.iterator(); it.hasNext(); ) {
					eventTypeId = it.next();
					eventName = ComponentManager.getInstance().getEventTypeManager().getEventType(eventTypeId).getName();
					if(LOG.isDebugEnabled()) {
						LOG.debug("Found event name [" + eventName + "] for event type id [" + eventTypeId + "]");
					}
					availableEvents.put(eventTypeId, eventName);
				}
			}
			
		}
		return availableEvents;
	}

	/**
	 * @param availableEvents the availableEvents to set
	 */
	protected void setAvailableEvents(Map<Long, String> availableEvents) {
		this.availableEvents = availableEvents;
	}
}
