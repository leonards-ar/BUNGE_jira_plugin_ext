/*
 * File name: SAPWebServiceClientFunctionPluginFactory.java
 * Creation date: Jul 25, 2009 12:07:51 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.sapws;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ar.com.bunge.jira.plugin.workflow.utils.PluginUtils;
import ar.com.bunge.jira.plugin.workflow.utils.WorkflowUtils;
import ar.com.bunge.util.Utils;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("unchecked")
public class SAPWebServiceClientFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
	public static final String REQUEST_TEMPLATE_PARAM = "field.requestTemplate";
	public static final String WS_URL_PARAM = "field.url";
	public static final String WS_USERNAME_PARAM = "field.username";
	public static final String WS_PASSWORD_PARAM = "field.password";
	public static final String WS_BASIC_AUTHENTICATION_PARAM = "field.basicAuthentication";
	public static final String JIRA_RESPONSE_FIELD_PARAM = "field.responseField";
	public static final String JIRA_STATUS_FIELD_PARAM = "field.statusField";
	public static final String JIRA_MESSAGE_FIELD_PARAM = "field.messageField";
	public static final String JIRA_THROW_EX_PARAM = "field.throwException";
	public static final String TRUE_TEXT = "true";
	
	private static final Logger LOG = Logger.getLogger(SAPWebServiceClientFunctionPluginFactory.class);	

	/**
	 * 
	 * @param fieldManager
	 */
	public SAPWebServiceClientFunctionPluginFactory(FieldManager fieldManager) {
	}

	/**
	 * @param velocityParams
	 * @param descriptor
	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForEdit(java.util.Map, com.opensymphony.workflow.loader.AbstractDescriptor)
	 */
	protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Called getVelocityParamsForEdit method with parameters velocityParams " + velocityParams + " and descriptor " + (descriptor != null ? descriptor.getClass() : null) + " [" + descriptor + "]");
		}
        if (!(descriptor instanceof FunctionDescriptor)) {
        	String msg = "Descriptor must be an instance of com.opensymphony.workflow.loader.FunctionDescriptor and it is an instance of " + (descriptor != null ? descriptor.getClass() : null);
        	LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        if(LOG.isDebugEnabled()) {
        	LOG.debug("Function descriptor args " + functionDescriptor.getArgs());
        }
        
        velocityParams.put("requestTemplate", Utils.decode((String) functionDescriptor.getArgs().get(REQUEST_TEMPLATE_PARAM)));
        velocityParams.put("url", functionDescriptor.getArgs().get(WS_URL_PARAM));
        velocityParams.put("username", functionDescriptor.getArgs().get(WS_USERNAME_PARAM));
        velocityParams.put("password", functionDescriptor.getArgs().get(WS_PASSWORD_PARAM));
        velocityParams.put("basicAuthentication", functionDescriptor.getArgs().get(WS_BASIC_AUTHENTICATION_PARAM));
        velocityParams.put("responseField", getFieldByName(descriptor, JIRA_RESPONSE_FIELD_PARAM));
        velocityParams.put("statusField", getFieldByName(descriptor, JIRA_STATUS_FIELD_PARAM));
        velocityParams.put("messageField", getFieldByName(descriptor, JIRA_MESSAGE_FIELD_PARAM));
        velocityParams.put("throwException", functionDescriptor.getArgs().get(JIRA_THROW_EX_PARAM));		

        List<Field> fields = PluginUtils.getCopyToFields();
		if(LOG.isDebugEnabled()) {
			LOG.debug("Available fields: " + fields);
		}
		velocityParams.put("fieldList", Collections.unmodifiableList(fields));        
	}

	/**
	 * @param velocityParams
	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForInput(java.util.Map)
	 */
	protected void getVelocityParamsForInput(Map velocityParams) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Called getVelocityParamsForInput method with parameters velocityParams " + velocityParams);
		}		
		List<Field> fields = PluginUtils.getCopyToFields();
		if(LOG.isDebugEnabled()) {
			LOG.debug("Available fields: " + fields);
		}
		velocityParams.put("fieldList", Collections.unmodifiableList(fields));
	}

	/**
	 * @param velocityParams
	 * @param descriptor
	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForView(java.util.Map, com.opensymphony.workflow.loader.AbstractDescriptor)
	 */
	protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Called getVelocityParamsForView method with parameters velocityParams " + velocityParams + " and descriptor " + (descriptor != null ? descriptor.getClass() : null) + " [" + descriptor + "]");
		}
		
        if (!(descriptor instanceof FunctionDescriptor)) {
        	String msg = "Descriptor must be an instance of com.opensymphony.workflow.loader.FunctionDescriptor and it is an instance of " + (descriptor != null ? descriptor.getClass() : null);
        	LOG.error(msg);
            throw new IllegalArgumentException(msg);
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        if(LOG.isDebugEnabled()) {
        	LOG.debug("Function descriptor args " + functionDescriptor.getArgs());
        }
        
        velocityParams.put("url", functionDescriptor.getArgs().get(WS_URL_PARAM));
	}

	/**
	 * @param conditionParams
	 * @return
	 * @see com.atlassian.jira.plugin.workflow.WorkflowPluginFactory#getDescriptorParams(java.util.Map)
	 */
	public Map getDescriptorParams(Map conditionParams) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Called getDescriptorParams method with parameters conditionParams " + conditionParams);
		}		
        Map params = new HashMap();

        addParameterFromConditionParams(REQUEST_TEMPLATE_PARAM, "requestTemplate", params, conditionParams, true);
        addParameterFromConditionParams(WS_URL_PARAM, "url", params, conditionParams);
        addParameterFromConditionParams(WS_USERNAME_PARAM, "username", params, conditionParams);
        addParameterFromConditionParams(WS_PASSWORD_PARAM, "password", params, conditionParams);
        addParameterFromConditionParams(WS_BASIC_AUTHENTICATION_PARAM, "basicAuthentication", params, conditionParams);
        addParameterFromConditionParams(JIRA_RESPONSE_FIELD_PARAM, "responseField", params, conditionParams);
        addParameterFromConditionParams(JIRA_STATUS_FIELD_PARAM, "statusField", params, conditionParams);
        addParameterFromConditionParams(JIRA_MESSAGE_FIELD_PARAM, "messageField", params, conditionParams);
        addParameterFromConditionParams(JIRA_THROW_EX_PARAM, "throwException", params, conditionParams);

        return params;
	}

	/**
	 * 
	 * @param paramFieldName
	 * @param conditionParamFieldName
	 * @param params
	 * @param conditionParams
	 */
	private void addParameterFromConditionParams(String paramFieldName, String conditionParamFieldName, Map params, Map conditionParams) {
		addParameterFromConditionParams(paramFieldName, conditionParamFieldName, params, conditionParams, false);
	}

	/**
	 * 
	 * @param paramFieldName
	 * @param conditionParamFieldName
	 * @param params
	 * @param conditionParams
	 * @param encode
	 */
	private void addParameterFromConditionParams(String paramFieldName, String conditionParamFieldName, Map params, Map conditionParams, boolean encode) {
		try {
			String value = extractSingleParam(conditionParams, conditionParamFieldName);
	        if(LOG.isDebugEnabled()) {
	        	LOG.debug("Setting descriptor param [" + paramFieldName + "] with condition param [" + conditionParamFieldName + "] and value [" + value + "]");
	        }
	        
	        if(encode) {
	        	value = Utils.encode(value);
	        }
	        
	        params.put(paramFieldName, value);
		} catch(IllegalArgumentException ex) {
			LOG.warn(ex.getLocalizedMessage(), ex);
		}
	}

	/**
	 * 
	 * @param descriptor
	 * @param name
	 * @return
	 */
	private Field getFieldByName(AbstractDescriptor descriptor, String name) { 
		try {
			FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
			Map args = functionDescriptor.getArgs();
			String fieldKey = (String) args.get(name);

			return (Field) WorkflowUtils.getFieldFromKey(fieldKey);
		} catch(IllegalArgumentException ex) {
			LOG.info(ex.getLocalizedMessage());
			return null;
		}
	}	
}
