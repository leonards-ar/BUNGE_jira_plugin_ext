/*
 * File name: DumpDataFunctionPluginFactory.java
 * Creation date: Jul 25, 2009 12:24:42 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.dump;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

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
public class DumpDataFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory {
	private static final Logger LOG = Logger.getLogger(DumpDataFunctionPluginFactory.class);	

	/**
	 * 
	 * @param fieldManager
	 */
	public DumpDataFunctionPluginFactory(FieldManager fieldManager) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Called DumpDataFunctionPluginFactory constructor with fieldManager");
		}
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
	}

	/**
	 * @param velocityParams
	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForInput(java.util.Map)
	 */
	protected void getVelocityParamsForInput(Map velocityParams) {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Called getVelocityParamsForInput method with parameters velocityParams " + velocityParams);
		}		
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
            LOG.debug("Setting velocityParam [dumpFilePath] with arg [field.dumpFilePath] and value [" + functionDescriptor.getArgs().get("field.dumpFilePath") + "]");
        }
        velocityParams.put("dumpFilePath", functionDescriptor.getArgs().get("field.dumpFilePath"));
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

        String dumpFilePath = extractSingleParam(conditionParams, "dumpFilePath");
        if(LOG.isDebugEnabled()) {
        	LOG.debug("Setting descriptor param [field.dumpFilePath] with condition param [dumpFilePath] and value [" + dumpFilePath + "]");
        }
        params.put("field.dumpFilePath", dumpFilePath);

        return params;
	}

}
