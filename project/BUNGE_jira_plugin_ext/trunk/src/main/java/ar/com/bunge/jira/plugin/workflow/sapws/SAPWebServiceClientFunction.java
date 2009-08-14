/*
 * File name: SAPWebServiceClientFunction.java
 * Creation date: Jul 25, 2009 12:06:40 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.sapws;

import java.util.Map;

import org.apache.log4j.Logger;

import ar.com.bunge.jira.plugin.workflow.AbstractPreserveChangesPostFunction;
import ar.com.bunge.jira.plugin.workflow.utils.IssueUtils;
import ar.com.bunge.jira.plugin.workflow.utils.LogUtils;
import ar.com.bunge.jira.plugin.workflow.utils.WorkflowUtils;
import ar.com.bunge.sapws.client.SAPClientXmlRequest;
import ar.com.bunge.sapws.client.SAPClientXmlResponse;
import ar.com.bunge.sapws.client.SAPWSClient;
import ar.com.bunge.util.Utils;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("unchecked")
public class SAPWebServiceClientFunction extends AbstractPreserveChangesPostFunction {
	private static final Logger LOG = Logger.getLogger(SAPWebServiceClientFunction.class);	

	/**
	 * 
	 */
	public SAPWebServiceClientFunction() {
	}

	/**
	 * 
	 * @param transientVars
	 * @param args
	 * @param status
	 * @param message
	 * @param changeHolder
	 */
	private void setResponseStatusAndMessage(Map transientVars, Map args, String status, String message, IssueChangeHolder changeHolder) {
		String statusFieldName = getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.JIRA_STATUS_FIELD_PARAM);
		if(LOG.isDebugEnabled()) {
			LOG.debug("Trying to set status field [" + statusFieldName + "] with value [" + status + "]");
		}
		setFieldValue(transientVars, statusFieldName, Utils.truncate(status, 255), changeHolder);
		
		String messageFieldName = getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.JIRA_MESSAGE_FIELD_PARAM);
		if(LOG.isDebugEnabled()) {
			LOG.debug("Trying to set message field [" + messageFieldName + "] with value [" + message + "]");
		}
		setFieldValue(transientVars, messageFieldName, Utils.truncate(message, 255), changeHolder);
	}
	
	/**
	 * 
	 * @param transientVars
	 * @param fieldKey
	 * @param fieldValue
	 * @param changeHolder
	 */
	private void setFieldValue(Map transientVars, String fieldKey, String fieldValue, IssueChangeHolder changeHolder) {
		
		if(fieldKey != null) {
	        try {
				MutableIssue issue = (MutableIssue) transientVars.get("issue");
				WorkflowUtils.setFieldValue(issue, fieldKey, fieldValue, changeHolder);
				issue.store();
	        } catch(Throwable ex) {
	        	 LOG.error("Cannot set value [" + fieldValue + "] to field key [" + fieldKey + "]: " + ex, ex);
	        }
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
	 * @param holder
	 * @throws WorkflowException
	 * @see ar.com.bunge.jira.plugin.workflow.AbstractPreserveChangesPostFunction#executeFunction(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet, com.atlassian.jira.issue.util.IssueChangeHolder)
	 */
	protected void executeFunction(Map<String, Object> transientVars, Map<String, String> args, PropertySet ps, IssueChangeHolder holder) throws WorkflowException {
		if(LOG.isDebugEnabled()) {
			LOG.debug(LogUtils.dumpMap("args", args));
		}
		boolean throwExceptions = getArgAsBoolean(args, SAPWebServiceClientFunctionPluginFactory.JIRA_THROW_EX_PARAM);

		SAPClientXmlResponse response = null;
		try {
			Map<String, Object> context = IssueUtils.buildContext(transientVars, args, ps);
			if(LOG.isDebugEnabled()) {
				LOG.debug(LogUtils.dumpMap("context", context));
			}

			SAPWSClient client = createWSClient(args);

			if(LOG.isDebugEnabled()) {
				LOG.debug(client);
			}

			SAPClientXmlRequest request = new SAPClientXmlRequest(getWSRequest(args));

			if(LOG.isDebugEnabled()) {
				LOG.debug(request);
			}
			
			response = client.execute(request, context);

			if(LOG.isDebugEnabled()) {
				LOG.debug(response);
			}
			
			setResponseStatusAndMessage(transientVars, args, response.getNumberAsString(), response.getMessage(), holder);
			setFieldValue(transientVars, getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.JIRA_RESPONSE_FIELD_PARAM), response.getResponse(), holder);
		} catch(Exception ex) {
			LOG.error("Could not execute web service: " + ex.getMessage(), ex);
			setResponseStatusAndMessage(transientVars, args, "-1", ex.getMessage(), holder);
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
	 * @param args
	 * @return
	 */
	private String getWSRequest(Map<String, String> args) {
		String request = Utils.decode(getArgAsString(args, SAPWebServiceClientFunctionPluginFactory.REQUEST_TEMPLATE_PARAM));
		return request != null ? request.trim() : null; 
	}
}
