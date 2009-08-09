/*
 * File name: SAPWebServiceClientFunction.java
 * Creation date: Jul 25, 2009 12:06:40 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.sapws;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import ar.com.bunge.sapws.client.SAPClientXmlRequest;
import ar.com.bunge.sapws.client.SAPClientXmlResponse;
import ar.com.bunge.sapws.client.SAPWSClient;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since SPM 1.0
 *
 */
@SuppressWarnings("unchecked")
public class SAPWebServiceClientFunction implements FunctionProvider {
	public static final String ISSUE_NAMESPACE = "issue";
	public static final String ISSUE_CUSTOM_TYPE_NAMESPACE = ISSUE_NAMESPACE + ".custom";
	
	private static final Logger LOG = Logger.getLogger(SAPWebServiceClientFunction.class);	

	/**
	 * 
	 */
	public SAPWebServiceClientFunction() {
	}

	/**
	 * @param transientVars
	 * @param args
	 * @param ps
	 * @throws WorkflowException
	 * @see com.opensymphony.workflow.FunctionProvider#execute(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet)
	 */
	public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		if(LOG.isDebugEnabled()) {
			LOG.debug(dumpMap("args", args));
		}
		boolean throwExceptions = getArgAsBoolean(args, SAPWebServiceClientFunctionPluginFactory.JIRA_THROW_EX_PARAM);

		SAPClientXmlResponse response = null;
		try {
			Map<String, Object> context = buildContext(transientVars, args, ps);
			if(LOG.isDebugEnabled()) {
				LOG.debug(dumpMap("context", context));
			}

			SAPWSClient client = createWSClient(args);

			if(LOG.isDebugEnabled()) {
				LOG.debug(client);
			}

			SAPClientXmlRequest request = new SAPClientXmlRequest(getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.REQUEST_TEMPLATE_PARAM));

			if(LOG.isDebugEnabled()) {
				LOG.debug(request);
			}
			
			response = client.execute(request, context);

			if(LOG.isDebugEnabled()) {
				LOG.debug(response);
			}
			
			setResponseStatusAndMessage(transientVars, args, response.getNumberAsString(), response.getMessage());
			setCustomFieldValue(transientVars, getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.JIRA_RESPONSE_FIELD_PARAM), response.getResponse());
		} catch(Exception ex) {
			LOG.error("Could not execute web service: " + ex.getMessage(), ex);
			setResponseStatusAndMessage(transientVars, args, "-1", ex.getMessage());
			if(throwExceptions) {
				throw new WorkflowException("Could not execute web service: " + ex.getMessage(), ex);
			}
		} finally {
			if(response != null && !response.isSuccess() && throwExceptions) {
				throw new WorkflowException(response.getMessage() + " (Number: " + response.getNumberAsString() + ")");
			}
		}
	}

	/**
	 * 
	 * @param transientVars
	 * @param args
	 * @param status
	 * @param message
	 */
	private void setResponseStatusAndMessage(Map transientVars, Map args, String status, String message) {
		String statusFieldName = getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.JIRA_STATUS_FIELD_PARAM);
		if(LOG.isDebugEnabled()) {
			LOG.debug("Trying to set status field [" + statusFieldName + "] with value [" + status + "]");
		}
		setCustomFieldValue(transientVars, statusFieldName, status);
		
		String messageFieldName = getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.JIRA_MESSAGE_FIELD_PARAM);
		if(LOG.isDebugEnabled()) {
			LOG.debug("Trying to set message field [" + messageFieldName + "] with value [" + message + "]");
		}
		setCustomFieldValue(transientVars, messageFieldName, message);
	}
	
	/**
	 * 
	 * @param transientVars
	 * @param fieldName
	 * @param fieldValue
	 */
	private void setCustomFieldValue(Map transientVars, String fieldName, String fieldValue) {
		if(fieldName != null) {
			
		} else {
			LOG.info("Cannot set custom field value for a null field name.");
		}
	}
	
	/**
	 * 
	 * @param args
	 * @return
	 */
	private SAPWSClient createWSClient(Map args) {
		SAPWSClient client = new SAPWSClient();
		client.setBasicAuthentication(true);
		client.setUrl(getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.WS_URL_PARAM));
		client.setUsername(getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.WS_USERNAME_PARAM));
		client.setPassword(getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.WS_PASSWORD_PARAM));
		return client;
	}
	
	/**
	 * 
	 * @return
	 */
	private String getArgAsString(Map args, String argName) {
		if(argName != null && args != null && args.containsKey(argName)) {
			return args.get(argName).toString();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @return
	 */
	private boolean getArgAsBoolean(Map args, String argName) {
		String val = getArgAsString(args, argName);
		return val != null && SAPWebServiceClientFunctionPluginFactory.TRUE_TEXT.equalsIgnoreCase(val);
	}
	
	/**
	 * 
	 * @param transientVars
	 * @param args
	 * @param ps
	 * @return
	 */
	private Map<String, Object> buildContext(Map transientVars, Map args, PropertySet ps) {
		Map<String, Object> context = new HashMap<String, Object>();
		
		Object issue = transientVars.get("issue");
		if(LOG.isDebugEnabled()) {
			LOG.debug("About to build context from issue [" + (issue != null ? issue.getClass() : "null") + "]");
		}

		if(issue instanceof Issue) {
			buildContextFromIssue(context, (Issue) issue);
		}
		
		return context;
	}
	
	/**
	 * 
	 * @param context
	 * @param issue
	 */
	private void buildContextFromIssue(Map<String, Object> context, Issue issue) {
		// Issue fields
		buildContextFromGenericValue(context, ISSUE_NAMESPACE, issue.getGenericValue());

		// Custom Fields
		List customFieldObjs = ManagerFactory.getCustomFieldManager().getCustomFieldObjects(issue);
		if(customFieldObjs != null) {
			CustomField cf;
			Object value;
			String namespace;
			for(Iterator it = customFieldObjs.iterator(); it.hasNext(); ) {
				cf = (CustomField) it.next();
				value = cf.getValue(issue);
				namespace = ISSUE_CUSTOM_TYPE_NAMESPACE + "." + cf.getName();
				if(LOG.isDebugEnabled()) {
					LOG.debug("Found custom field [" + cf.getName() + "] of type [" + cf.getCustomFieldType().getKey() + " / " + cf.getCustomFieldType().getName() + "] with value [" + value + "] of class [" + (value != null ? value.getClass() : "null") + "]" );
				}
				context.put(namespace, getCustomFieldValue(value, cf.getCustomFieldType()));
			}
		}
	}
	
	/**
	 * 
	 * @param context
	 * @param namespace
	 * @param genericValue
	 */
	private void buildContextFromGenericValue(Map<String, Object> context, String namespace, GenericValue genericValue) {
		Object key, value;
		for(Iterator it = genericValue.getAllKeys().iterator(); it.hasNext(); ) {
			key = it.next();
			value = genericValue.get(key);
			
			if(LOG.isDebugEnabled()) {
				LOG.debug("Found generic value field [" + key + "] with value [" + value + "] of class [" + (value != null ? value.getClass() : "null") + "]" );
			}
			
			context.put(namespace + "." + key, getGenericValueFieldValue(value));
		}
		
	}
	
	/**
	 * 
	 * @param value
	 * @param type
	 * @return
	 */
	private Object getCustomFieldValue(Object value, CustomFieldType type) {
		// For the moment no special treatment!
		return getGenericValueFieldValue(value);
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	private Object getGenericValueFieldValue(Object value) {
		if(value instanceof Number) {
			return value;
		} else if(value instanceof Date) {
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			return df.format((Date) value);
		} else if(value instanceof Collection) {
			Collection c = (Collection) value;
			return c.size() > 0 ? getGenericValueFieldValue(c.iterator().next()) : null;
		} else if(value != null) {
			return value.toString();
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param description
	 * @param map
	 * @return
	 */
	private String dumpMap(String description, Map map) {
		if(map != null && map.size() > 0) {
			Object aKey, aValue;
			StringBuffer s = new StringBuffer(description + ": {");
			for(Iterator it = map.keySet().iterator(); it.hasNext(); ) {
				aKey = it.next();
				aValue = map.get(aKey);
				s.append(aKey.getClass().getName() + ":[" + aKey + "]");
				s.append(" = ");
				s.append(aValue.getClass().getName() + ":[" + aValue + "]");
				if(it.hasNext()) {
					s.append(",\n\t");
				}
			}
			s.append("}");
			return s.toString();
		} else if(map != null && map.size() <= 0) {
			return description + ": Empty map";
		} else {
			return description + ": Null map";
		}		
	}
}
