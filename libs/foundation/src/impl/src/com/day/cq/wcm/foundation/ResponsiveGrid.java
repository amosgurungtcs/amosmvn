/**
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2014 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */
package com.day.cq.wcm.foundation;

import com.adobe.cq.sightly.WCMUsePojo;
import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.TemplatedResource;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.api.designer.ComponentStyle;
import com.day.cq.wcm.api.designer.Style;
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Use API for the responsive grid sightly component
 * /libs/wcm/foundation/components/responsivegrid
 */
public class ResponsiveGrid extends WCMUsePojo {

    // default config
    private final String DEFAULT_CSS_PREFIX = "aem-Grid";

    private static final int DEFAULT_COLUMNS = 12;
    private static final String BREAKPOINT_VARIANT_NAME_DEFAULT = "default";

    // private
    private String cssClassPrefix;

    // exposed
    private int columns;

    private String cssClass;

    private List<Column> paragraphs = new ArrayList<Column>();

    /**
     * Class to get the necessary properties for a column in the grid
     */
    public class Column {

        private Resource resource;
        private Map<String, Integer> gridBreakpoints;
        private String cssClass;
        private Map<String, Integer> breakpoints;
        private Set<String> columnOnlyBreakpointNames;

        private String columnCssClassPrefix;

        public Column(Resource res) {
            this.resource = res;
            this.columnCssClassPrefix = cssClassPrefix + "Column";

            createCssClass();
        }

        public Column(Resource res, Map<String, Integer> gridBreakpoints) {
            this.resource = res;
            this.gridBreakpoints = gridBreakpoints;
            this.columnCssClassPrefix = cssClassPrefix + "Column";

            createCssClass();
        }

        public Resource getResource() { return resource; }

        public String getPath() { return resource.getPath(); }

        public Set<String> getMissingBreakpointNames() {
            return columnOnlyBreakpointNames;
        }

        public Integer getColumnCount(String breakpointName) {
            return breakpoints.get(breakpointName);
        }

        private void createCssClass() {
            String cssClass = columnCssClassPrefix;
            this.columnOnlyBreakpointNames = new HashSet<String>();

            Component component = WCMUtils.getComponent(resource);
            ValueMap componentProperties = null;

            if (component != null) {
                componentProperties = component.getProperties();
            }

            Resource responsiveCfg = resource.getChild(NameConstants.NN_RESPONSIVE_CONFIG);
            breakpoints = new HashMap<String, Integer>();
            if (responsiveCfg != null) {
                for (Iterator<Resource> resCfgIt = responsiveCfg.listChildren(); resCfgIt.hasNext();) {
                    Resource resCfg = resCfgIt.next();
                    String breakpointName = resCfg.getName();
                    ValueMap cfg = resCfg.adaptTo(ValueMap.class);

                    int width = cfg.get("width", 0);
                    String behavior = cfg.get("behavior", String.class);

                    breakpoints.put(breakpointName, width);

                    if (width > 0) {
                        cssClass += " " + columnCssClassPrefix + "--" + breakpointName + "--" + width;
                    }

                    if (behavior != null) {
                        cssClass += " " + columnCssClassPrefix + "--" + breakpointName + "--" + behavior;
                    }
                }
            }

            if (gridBreakpoints != null && !gridBreakpoints.isEmpty()) {
                // Missing breakpoints originating from the parent
                List<String> missingGridBreakpointNames = new ArrayList<String>(gridBreakpoints.keySet());
                missingGridBreakpointNames.removeAll(breakpoints.keySet());

                for (String missingGridBreakpointName: missingGridBreakpointNames) {
                    // Get the default width
                    Integer width = breakpoints.get(BREAKPOINT_VARIANT_NAME_DEFAULT);
                    Integer gridWidth = gridBreakpoints.get(missingGridBreakpointName);

                    if (width == null || (gridWidth != null && width > gridWidth)) {
                        // Get the parent corresponding width
                        width = gridWidth;
                    }

                    if (width == null || width > DEFAULT_COLUMNS) {
                        width = DEFAULT_COLUMNS;
                    }

                    cssClass += " " + columnCssClassPrefix + "--" + missingGridBreakpointName + "--" + width;
                }

                List<String> missingBreakpointNames = new ArrayList<String>(breakpoints.keySet());
                missingBreakpointNames.removeAll(gridBreakpoints.keySet());

                for (String missingBreakPointName : missingBreakpointNames) {
                    columnOnlyBreakpointNames.add(missingBreakPointName);
                }
            } else {
                // The parent doesn't have the current column breakpoints
                columnOnlyBreakpointNames.addAll(breakpoints.keySet());
            }

            if (!breakpoints.keySet().contains(BREAKPOINT_VARIANT_NAME_DEFAULT)) {
                cssClass += " " + columnCssClassPrefix + "--" + BREAKPOINT_VARIANT_NAME_DEFAULT + "--" + DEFAULT_COLUMNS;
            }

            if (componentProperties != null) {
                cssClass += " " + componentProperties.get(ComponentStyle.PN_CSS_CLASS, "");
            }

            this.cssClass = cssClass;
        }

        public String getCssClass() {
            return this.cssClass;
        }

        public ValueMap getProperties() { return resource.adaptTo(ValueMap.class); }
    }

    @Override
    public void activate() throws Exception {
        Style currentStyle = getCurrentStyle();
        ValueMap properties = this.getProperties();
        Map<String, Integer> breakpoints = new HashMap<String, Integer>();
        Set<String> missingParagraphBreakpointNames = new HashSet<String>();

        // class prefix
        cssClassPrefix = currentStyle.get("cssPrefix", DEFAULT_CSS_PREFIX);

        // grid columns css
        cssClass = DEFAULT_CSS_PREFIX;

        // columns
        Resource responsiveParentCfg = getResource().getParent().getChild(NameConstants.NN_RESPONSIVE_CONFIG);
        Resource responsiveCfg = getResource().getChild(NameConstants.NN_RESPONSIVE_CONFIG);
        boolean hasResponsiveCfgWidth = false;
        if (responsiveCfg != null) {
            for (Iterator<Resource> resCfgIt = responsiveCfg.listChildren(); resCfgIt.hasNext();) {
                Resource resCfg = resCfgIt.next();
                String breakpointName = resCfg.getName();
                ValueMap cfg = resCfg.adaptTo(ValueMap.class);

                int width = cfg.get("width", 0);
                if (responsiveParentCfg != null) {
                    Resource parentBreakpoint = responsiveParentCfg.getChild(breakpointName);

                    // The width of the responsive grid cannot be bigger than the one of its parent
                    if (parentBreakpoint != null) {
                        ValueMap parentCfg = parentBreakpoint.adaptTo(ValueMap.class);
                        int parentWidth = parentCfg.get("width", width);
                        width = width > parentWidth ? parentWidth : width;
                    }
                }

                breakpoints.put(breakpointName, width);

                if (width > 0) {
                    hasResponsiveCfgWidth = true;
                    cssClass += " " + DEFAULT_CSS_PREFIX + "--" + breakpointName + "--" + width;
                }
            }
        }

        if (!hasResponsiveCfgWidth) {
            int columns = currentStyle.get("columns", DEFAULT_COLUMNS);
            cssClass += " " + DEFAULT_CSS_PREFIX + "--" + columns;
        }

        if (!breakpoints.containsKey(BREAKPOINT_VARIANT_NAME_DEFAULT)) {
            int columns = currentStyle.get("columns", DEFAULT_COLUMNS);
            breakpoints.put(BREAKPOINT_VARIANT_NAME_DEFAULT, columns);
            cssClass += " " + DEFAULT_CSS_PREFIX + "--" + BREAKPOINT_VARIANT_NAME_DEFAULT + "--" + DEFAULT_COLUMNS;
        }

        for (Iterator<Resource> paragraphsIt = getEffectiveResource().listChildren(); paragraphsIt.hasNext();) {
            Resource child = paragraphsIt.next();
            if (!NameConstants.NN_RESPONSIVE_CONFIG.equals(child.getName())) {
                Column column;
                if (child instanceof TemplatedResource) {
                    column = new Column(((TemplatedResource) child).getResource(), breakpoints);
                } else {
                    column = new Column(child, breakpoints);
                }
                paragraphs.add(column);

                // Add missing breakpoints originating from the children
                missingParagraphBreakpointNames.addAll(column.getMissingBreakpointNames());
            }
        }

        // In case a paragraph has a breakpoint that the grid doesn't
        // Add that breakpoint to the parent with the default value
        if (!missingParagraphBreakpointNames.isEmpty()) {
            for (String missingParagraphBreakpointName : missingParagraphBreakpointNames) {
                Integer columnCount = breakpoints.get(BREAKPOINT_VARIANT_NAME_DEFAULT);

                if (columnCount == null) {
                    columnCount = currentStyle.get("columns", DEFAULT_COLUMNS);
                }

                cssClass += " " + DEFAULT_CSS_PREFIX + "--" + missingParagraphBreakpointName + "--" + columnCount;
            }
        }

        // add custom styles of the resource
        cssClass += " " + properties.get(ComponentStyle.PN_CSS_CLASS, "");
    }

    /**
     * get the resource for displaying the parsys, handle templated content
     * @param <T>
     * @return
     */
    public <T extends Resource> T getEffectiveResource() {
        Resource templatedResource = getRequest().adaptTo(TemplatedResource.class);
        if (templatedResource == null) {
            return (T) getResource();
        } else {
            return (T) templatedResource;
        }
    }

    /**
     *
     * @return css classes to be added to the markup of the grid container
     */
    public String getCssClass() {
        return cssClass;
    }

    /**
     *
     * @return all columns added to the grid
     */
    public List<Column> getParagraphs() {
        return paragraphs;
    }
}
