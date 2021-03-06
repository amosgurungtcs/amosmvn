/*
 * Copyright 1997-2008 Day Management AG
 * Barfuesserplatz 6, 4001 Basel, Switzerland
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Day Management AG, ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Day.
 */
package com.day.cq.wcm.foundation.impl;

import com.day.cq.statistics.StatisticsService;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.core.stats.PageView;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Servlet tracks page-impression, can be used to be requested via client-side JS
 */
@Component(metatype = true, policy = ConfigurationPolicy.OPTIONAL, label = "Adobe Page Impressions Tracker",
        description = "Adobe Page Impressions configuration component.")
@Service(Servlet.class)
@Properties({
        @Property(name = "sling.servlet.extensions", value = "js", propertyPrivate = true),
        @Property(name = "sling.servlet.paths", value = "/libs/wcm/stats/tracker", propertyPrivate = true),
        @Property(name = "sling.auth.requirements", value = "", propertyPrivate = false)
})
public class PageImpressionsTracker extends SlingSafeMethodsServlet {

    private static final long serialVersionUID = -7915330057389423541L;
    private static final Logger log = LoggerFactory.getLogger(PageImpressionsTracker.class);
    private static final String PATH_PARAMETER = "path";
    private static final String STATISTICS_PATH = "/pages";
    @Reference
    private StatisticsService statisticsService;
    private String statisticsPath;

    /**
     * Writes down a View for a Page. The Pages handle is passed via
     * request-parameter with name {@link #PATH_PARAMETER "path"}
     * If there is no {@link com.day.cq.wcm.api.Page Page}
     * at that path the count is ignored
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    @SuppressWarnings({"JavaDoc"})
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        RequestParameter[] parameter = request.getRequestParameters(PATH_PARAMETER);
        response.setContentType("text/javascript");
        response.setHeader("cache-control", "no-store");
        if (parameter != null) {
            ResourceResolver resolver = request.getResourceResolver();
            for (RequestParameter path : parameter) {
                try {
                    Resource resource = resolver.getResource(path.getString());
                    if (resource != null && resource.adaptTo(Page.class) != null) {
                        PageView view = new PageView(statisticsPath, resource.adaptTo(Page.class), WCMMode.DISABLED);
                        statisticsService.addEntry(view);
                    }
                } catch (RepositoryException e) {
                    log.error("Failed to add entry for " + path.getString(), e);
                }
            }
        }
        response.getWriter().println("//impression added");
    }

    @Activate
    protected void activate(ComponentContext ctx) {
        statisticsPath = statisticsService.getPath() + STATISTICS_PATH;
    }
}
