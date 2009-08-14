/*
 * File name: LogUtils.java
 * Creation date: Aug 14, 2009 4:41:42 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.utils;

import java.util.Iterator;
import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.user.UserEvent;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("unchecked")
public class LogUtils {

	/**
	 * 
	 */
	private LogUtils() {
	}

	/**
	 * 
	 * @param description
	 * @param map
	 * @return
	 */
	public static String dumpMap(String description, Map map) {
		if(map != null && map.size() > 0) {
			Object aKey, aValue;
			StringBuffer s = new StringBuffer(description + ": {");
			for(Iterator it = map.keySet().iterator(); it.hasNext(); ) {
				aKey = it.next();
				aValue = map.get(aKey);
				s.append(aKey.getClass().getName() + ":[" + aKey + "]");
				s.append(" = ");
				if(aValue != null) {
					s.append(aValue.getClass().getName() + ":[" + aValue + "]");
				} else {
					s.append("[" + aValue + "]");
				}
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

	/**
	 * 
	 * @param description
	 * @param event
	 * @return
	 */
	public static String dumpEvent(String description, JiraEvent event) {
        try {
        	StringBuffer s = new StringBuffer(description + ":\n");
            if (event instanceof IssueEvent) {
                IssueEvent issueEvent = (IssueEvent) event;
                s.append("\tIssue: [#" + issueEvent.getIssue().getLong("id") + "] " + issueEvent.getIssue().getString("summary"));
                s.append("\n\tComment: " + issueEvent.getComment());
                s.append("\n\tChange Group: " + issueEvent.getChangeLog());
                s.append("\n\tEvent Type: " + ComponentManager.getInstance().getEventTypeManager().getEventType(issueEvent.getEventTypeId()).getName());
            } else if (event instanceof UserEvent) {
                UserEvent userEvent = (UserEvent) event;
                s.append("\tUser: " + userEvent.getUser().getName() + " (" + userEvent.getUser().getEmail() + ")");
            }

            s.append("\n\tTime: " + event.getTime());
            return s.toString();
        } catch (Exception ex) {
        	return ex.getClass().getName() + " -> " + ex.getLocalizedMessage();
        }
    }	
}
