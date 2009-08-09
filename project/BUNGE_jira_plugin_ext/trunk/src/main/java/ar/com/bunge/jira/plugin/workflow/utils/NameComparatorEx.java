/*
 * File name: NameComparatorEx.java
 * Creation date: Aug 9, 2009 4:25:40 PM
 * Copyright Mindpool
 */
package ar.com.bunge.jira.plugin.workflow.utils;

import java.util.Comparator;

import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.web.bean.I18nBean;

/**
 *
 * @author Gustavo Martin
 * 
 * This Comparator is used to compare two fields by its internationalized name.
 * 
 */
public class NameComparatorEx implements Comparator<Field> {
    private final I18nBean i18nBean;

    /**
     * 
     * @param i18nBean
     */
    public NameComparatorEx(I18nBean i18nBean) {
        this.i18nBean = i18nBean;
    }

    /**
     * 
     * @param o1
     * @param o2
     * @return
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(Field o1, Field o2) {
        if (o1 == null)
            throw new IllegalArgumentException("The first parameter is null");
        if (o2 == null)
            throw new IllegalArgumentException("The second parameter is null");

        String name1 = i18nBean.getText(o1.getName());
        String name2 = i18nBean.getText(o2.getName());

        return name1.compareTo(name2);
    }

}
