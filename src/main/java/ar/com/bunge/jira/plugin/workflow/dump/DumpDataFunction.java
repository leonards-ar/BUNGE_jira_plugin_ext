/*
 * File name: DumpDataFunction.java
 * Creation date: Jul 25, 2009 12:24:06 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.dump;

import java.io.FileOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import ar.com.bunge.jira.plugin.workflow.utils.LogUtils;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.WorkflowException;

/**
 *
 * @author <a href="mcapurro@gmail.com">Mariano Capurro</a>
 * @version 1.0
 * @since 1.0
 *
 */
@SuppressWarnings("unchecked")
public class DumpDataFunction implements FunctionProvider {
	private static final Logger LOG = Logger.getLogger(DumpDataFunction.class);	

	/**
	 * 
	 */
	public DumpDataFunction() {
		if(LOG.isDebugEnabled()) {
			LOG.debug("Called default DumpDataFunction constructor");
		}
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
			LOG.debug("Called execute method with parameters transientVars " + transientVars + ", args " + args + " and property set " + ps);
		}
	
		String dumpFilePath = args.containsKey("field.dumpFilePath") ? args.get("field.dumpFilePath").toString() : null;
		
		if(LOG.isDebugEnabled()) {
			LOG.debug("Dumping function call to file [" + dumpFilePath + "]");
		}
		FileOutputStream dumpFileStream = null;
		
    	try {
    		dumpFileStream = new FileOutputStream( dumpFilePath, true );
    		dumpFileStream.write( ("*** Starting new Dump [" + new Date() + "] ***\n\n").getBytes() );
    		
    		if(LOG.isDebugEnabled()) {
        		LOG.debug(LogUtils.dumpMap("transientVars", transientVars));
    		}
    		dumpFileStream.write(LogUtils.dumpMap("transientVars", transientVars).getBytes());
    		dumpFileStream.write("\n".getBytes());

    		if(LOG.isDebugEnabled()) {
        		LOG.debug(dumpIssue("issue", transientVars.get("issue")));
    		}
    		dumpFileStream.write(dumpIssue("issue", transientVars.get("issue")).getBytes());
    		dumpFileStream.write("\n".getBytes());
    		
    		if(LOG.isDebugEnabled()) {
        		LOG.debug(LogUtils.dumpMap("args", args));
    		}    		
    		dumpFileStream.write(LogUtils.dumpMap("args", args).getBytes());
    		dumpFileStream.write( "\n".getBytes() );
    		
    		if(LOG.isDebugEnabled()) {
        		LOG.debug(dumpPropertySet("propertySet", ps));
    		}
    		dumpFileStream.write(dumpPropertySet("propertySet", ps).getBytes());
    		dumpFileStream.write( "\n".getBytes() );
    		
    		dumpFileStream.flush();
    		
    		if(LOG.isDebugEnabled()) {
    			LOG.debug("Finished dumping data");
    		}
    	} catch(Throwable ex) {
    		LOG.error("Could not dump function data to file [" + dumpFilePath + "]", ex);
    	    throw new WorkflowException("Could not dump function data to file [" + dumpFilePath + "]. (" + ex.getClass() + ")", ex);
    	} finally {
    	    if( dumpFileStream != null ) {
    	        try {
    	        	dumpFileStream.close();
    	        } catch( Exception ex ) {
    	        	LOG.error("Could not close file stream for file [" + dumpFilePath + "]", ex);
    	        }
    	    }		
    	}
	}

	/**
	 * 
	 * @param description
	 * @param issue
	 * @return
	 */
	private String dumpIssue(String description, Object issue) {
		StringBuffer s = new StringBuffer(description + ": {");
		GenericValue i = null;
		
		if(issue instanceof Issue) {
			Issue io = (Issue) issue;
			List customFieldObjs = ManagerFactory.getCustomFieldManager().getCustomFieldObjects(io);
			s.append("customFields: [");
			if(customFieldObjs != null) {
				CustomField cf;
				for(Iterator it = customFieldObjs.iterator(); it.hasNext(); ) {
					cf = (CustomField) it.next();
					s.append(cf.getName() + ": ");
					s.append(cf.getValue(io) + " (");
					s.append(cf.getCustomFieldType().getKey() + " / " + cf.getCustomFieldType().getName());
					s.append(")");
					if(it.hasNext()) {
						s.append(", ");
					}
					
				}
			}
			s.append("]");
			
			Project p = io.getProjectObject();
			if(p != null) {
				s.append("\nProject: [");
				s.append(p.getName());
				s.append("]");
			}
		} else if(issue instanceof GenericValue) {
			i = (GenericValue) issue;
		} else if(issue == null) {
			s.append("Null issue");
		} else {
			s.append("Issue class [" + issue.getClass() + "] -> " + i);
		}
		
		if(i != null) {
			Object aKey, aValue;
			for(Iterator it = i.getAllKeys().iterator(); it.hasNext(); ) {
				aKey = it.next();
				aValue = i.get(aKey);
				s.append(aKey.getClass().getName() + ":[" + aKey + "]");
				s.append(" = ");
				if(aValue != null) {
					s.append(aValue.getClass().getName() + ":[" + aValue + "]");
				} else {
					s.append("[null]");
				}
				if(it.hasNext()) {
					s.append(",\n\t");
				}
			}
			
		}
		
		s.append("}");
		return s.toString();
	}
		
	/**
	 * 
	 * @param description
	 * @param map
	 * @return
	 */
	private String dumpPropertySet(String description, PropertySet ps) {
		if(ps != null && ps.getKeys() != null && ps.getKeys().size() > 0) {
			Object aKey, aValue;
			StringBuffer s = new StringBuffer(description + ": {");
			for(Iterator it = ps.getKeys().iterator(); it.hasNext(); ) {
				aKey = it.next();
				aValue = ps.getObject(aKey.toString());
				s.append(aKey.getClass().getName() + ":[" + aKey + "]");
				s.append(" = ");
				s.append(aValue.getClass().getName() + ":[" + aValue + "]");
				if(it.hasNext()) {
					s.append(",\n\t");
				}
			}
			s.append("}");
			return s.toString();
		} else if(ps != null && ps.getKeys() != null && ps.getKeys().size() <= 0) {
			return description + ": Empty property set";
		} else {
			return description + ": Null property set or keys collections";
		}		
	}	
}
