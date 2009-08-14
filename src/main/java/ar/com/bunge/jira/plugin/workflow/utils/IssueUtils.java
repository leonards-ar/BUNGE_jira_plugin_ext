/*
 * File name: IssueUtils.java
 * Creation date: Aug 14, 2009 6:35:06 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.utils;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.module.propertyset.PropertySet;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("unchecked")
public class IssueUtils {
	private static final Logger LOG = Logger.getLogger(IssueUtils.class);
	public static final String ISSUE_NAMESPACE = "issue";
	public static final String ISSUE_CUSTOM_TYPE_NAMESPACE = ISSUE_NAMESPACE + ".custom";

	/**
	 * 
	 */
	private IssueUtils() {
	}

	/**
	 * 
	 * @param transientVars
	 * @param args
	 * @param ps
	 * @return
	 */
	public static Map<String, Object> buildContext(Map transientVars, Map args, PropertySet ps) {
		Object issue = transientVars.get("issue");

		if(LOG.isDebugEnabled()) {
			LOG.debug("About to build context from issue [" + (issue != null ? issue.getClass() : "null") + "]");
		}

		if(issue instanceof Issue) {
			return buildContext((Issue) issue);
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param issue
	 * @return
	 */
	public static Map<String, Object> buildContext(Issue issue) {
		Map<String, Object> context = new HashMap<String, Object>();

		buildContextFromIssue(context, issue);
		
		return context;
		
	}
	
	/**
	 * 
	 * @param context
	 * @param issue
	 */
	private static void buildContextFromIssue(Map<String, Object> context, Issue issue) {
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
	private static void buildContextFromGenericValue(Map<String, Object> context, String namespace, GenericValue genericValue) {
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
	private static Object getCustomFieldValue(Object value, CustomFieldType type) {
		// For the moment no special treatment!
		return getGenericValueFieldValue(value);
	}
	
	/**
	 * 
	 * @param value
	 * @return
	 */
	private static Object getGenericValueFieldValue(Object value) {
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
}
