/*
 * File name: SAPWebServiceClientFunctionPluginFactory.java
 * Creation date: Jul 25, 2009 12:07:51 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.sapws;

import java.util.Map;

import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
import com.atlassian.jira.plugin.workflow.WorkflowPluginFunctionFactory;
import com.opensymphony.workflow.loader.AbstractDescriptor;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since SPM 1.0
 *
 */
public class SAPWebServiceClientFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {

	/**
	 * 
	 */
	public SAPWebServiceClientFunctionPluginFactory() {
	}

	/**
	 * @param velocityParams
	 * @param descriptor
	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForEdit(java.util.Map, com.opensymphony.workflow.loader.AbstractDescriptor)
	 */
	protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor) {
	}

	/**
	 * @param velocityParams
	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForInput(java.util.Map)
	 */
	protected void getVelocityParamsForInput(Map velocityParams) {
	}

	/**
	 * @param velocityParams
	 * @param descriptor
	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForView(java.util.Map, com.opensymphony.workflow.loader.AbstractDescriptor)
	 */
	protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor) {
	}

	/**
	 * @param conditionParams
	 * @return
	 * @see com.atlassian.jira.plugin.workflow.WorkflowPluginFactory#getDescriptorParams(java.util.Map)
	 */
	public Map getDescriptorParams(Map conditionParams) {
		return null;
	}

}
