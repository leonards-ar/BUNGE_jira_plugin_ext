/*
 * File name: AbstractPreserveChangesPostFunction.java
 * Creation date: Aug 9, 2009 4:34:11 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

/**
 * Abstract post-function with transparent change tracking.
 * 
 * @author <a href="mailto:abashev@gmail.com">Alexey Abashev</a>
 * @version $Id$
 */
public abstract class AbstractPreserveChangesPostFunction extends AbstractJiraFunctionProvider {
	private static final String CHANGE_ITEMS = "changeItems";
	
	protected final Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Mirror for execute method but with holder for changes
	 * 
	 * @param transientVars
	 * @param args
	 * @param ps
	 * @param holder
	 * @throws WorkflowException
	 */
	protected abstract void executeFunction(Map<String, Object> transientVars, Map<String, String> args, PropertySet ps, IssueChangeHolder holder) throws WorkflowException;
	
	/**
	 * 
	 * @param transientVars
	 * @param args
	 * @param ps
	 * @throws WorkflowException
	 * @see com.opensymphony.workflow.FunctionProvider#execute(java.util.Map, java.util.Map, com.opensymphony.module.propertyset.PropertySet)
	 */
	@SuppressWarnings("unchecked")
	public final void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
		IssueChangeHolder holder = createChangeHolder(transientVars);
		
		try {
			executeFunction(transientVars, args, ps, holder);
		} finally {
			releaseChangeHolder(holder, transientVars);
		}
	}
	
	/**
	 * Create new holder with changes from transient vars
	 * @param transientVars
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private IssueChangeHolder createChangeHolder(Map<String, Object> transientVars) {
		List<ChangeItemBean> changeItems = (List<ChangeItemBean>) transientVars.get(CHANGE_ITEMS);
		
        if (changeItems == null) {
            changeItems = new LinkedList<ChangeItemBean>();
        }

        if (log.isDebugEnabled()) {
			log.debug("Create new holder with items - " + changeItems.toString());
		}
        
        IssueChangeHolder holder = new DefaultIssueChangeHolder();
        
        holder.setChangeItems(changeItems);
        
        return holder;
	}
	
	/**
	 * Release holder for changes.
	 * @param holder
	 * @param transientVars
	 */
	@SuppressWarnings("unchecked")
	private void releaseChangeHolder(IssueChangeHolder holder, Map<String, Object> transientVars) {
		List<ChangeItemBean> items = holder.getChangeItems();
		
		if (log.isDebugEnabled()) {
			log.debug("Release holder with items - " + items.toString());
		}
		
		transientVars.put(CHANGE_ITEMS, items);
	}
}
