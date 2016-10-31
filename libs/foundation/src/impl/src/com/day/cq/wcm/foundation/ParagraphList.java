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
package com.day.cq.wcm.foundation;

import com.adobe.granite.xss.XSSAPI;
import com.day.cq.commons.feed.StringResponseWrapper;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.components.Component;
import com.day.cq.wcm.commons.WCMUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.wrappers.SlingHttpServletResponseWrapper;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;
import java.util.List;

/**
 * Exports a list of paragraphs.
 */
@org.apache.felix.scr.annotations.Component(metatype = false)
@Service(Servlet.class)
@Properties({
        @Property(name = "sling.servlet.paths", value = "/bin/wcm/foundation/paragraphlist"),
        @Property(name = "sling.servlet.extensions", value = {"html","json"}),
        @Property(name = "sling.servlet.methods", value = "GET")
})
public class ParagraphList extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    @Reference
    private XSSAPI xssAPI;

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2156140435583248698L;

    /** default start path */
    private static final String DEFAULT_START_PATH = "/content";

    /**
     * Query clause
     */
    public static final String QUERY = "query";

    /**
     * Result limit
     */
    public static final String LIMIT = "limit";

    /**
     * Start index
     */
    public static final String START = "start";

    /**
     * Default limit
     */
    public static final int DEFAULT_LIMIT = 20;

    @Override
    protected void doGet(
            SlingHttpServletRequest request, SlingHttpServletResponse response)
            throws ServletException, IOException {

        Page page = null;
        String resourceType = null;

        String queryString = request.getParameter(QUERY);
        if (queryString == null || queryString.length() == 0) {
            queryString = "";
        }
        queryString = queryString.trim();
        String[] queryTerms = queryString.split("\\s+");

        // set default start path
        RequestPathInfo pathInfo = request.getRequestPathInfo();
        // this doesn't work in classic ui, here we have to read out the parameter
        String paramPath = queryString.replace("path:", "");
        String path = pathInfo.getSuffix() != null ? pathInfo.getSuffix() :
                (paramPath != null) ? paramPath : DEFAULT_START_PATH;

        try {
            page = request.getResourceResolver().getResource(path).adaptTo(Page.class);
        } catch (Exception e) {
            log.error("Specified path is not a page.", e);
        }

        if (page != null) {
            Resource content = page.getContentResource();
            List<String> resourceTypes = new ArrayList<String>();

            for (int t = 0; t < queryTerms.length; t++) {
                String term = queryTerms[t].replace("\"", "");

                if (term.startsWith("sling:resourceType:")) {
                    resourceTypes.add(term.replace("sling:resourceType:", ""));
                }
            }

            if (content != null) {
                List<Map> paragraphs = getResults(content, request, response);
                Iterator<Map> paraIt = paragraphs.iterator();
                while(paraIt.hasNext()) {
                    String paraPath = (String)paraIt.next().get("path");
                    Resource res = request.getResourceResolver().getResource(paraPath);
                    if(res != null && resourceTypes.size() > 0) {
                        boolean isFiltered = false;
                        for (String rt : resourceTypes) {
                            if (rt != null && res.isResourceType(rt)) {
                                isFiltered = true;
                                break;
                            }
                        }

                        if(!isFiltered) {
                            paraIt.remove();
                        }
                    }
                }

                String startStr = request.getParameter(START);
                String limitStr = request.getParameter(LIMIT);
                int start = (startStr != null) ? Math.max(Integer.parseInt(startStr), 0) : 0;
                int limit = (limitStr != null) ? Math.max(Integer.parseInt(limitStr), 0) : DEFAULT_LIMIT;
                int end = start+limit;

                if (start < paragraphs.size()) {
                    paragraphs = paragraphs.subList(start, end > paragraphs.size() ? paragraphs.size() : end);
                } else {
                    paragraphs.clear();
                }

                if ("json".equals(request.getRequestPathInfo().getExtension())) {
                    JSONObject jsonResults = new JSONObject();
                    try {
                        response.setContentType("application/json");
                        response.setCharacterEncoding("utf-8");
                        jsonResults = generateJsonOutput(paragraphs);
                    } catch (JSONException e) {
                        throw new ServletException("Failed to produce JSON output", e);
                    } finally {
                        response.getWriter().write(jsonResults.toString());
                    }
                } else {
                    try {
                        response.getWriter().write(generateHtmlOutput(paragraphs, request, response));
                    } catch (Exception e) {
                        throw new ServletException("Failed to produce HTML output", e);
                    }
                }
            }
        }
    }

    void writeEmptyJSON(SlingHttpServletResponse response)
            throws ServletException, IOException {
    }

    protected List<Map> getResults(Resource content, SlingHttpServletRequest request, SlingHttpServletResponse response) {
        List<Map> results = new ArrayList<Map>();

        Iterator<Resource> iterator = content.getResourceResolver().listChildren(content);
        while (iterator.hasNext()) {
            Resource resource = iterator.next();
            Component component = WCMUtils.getComponent(resource);
            if (component != null && component.isContainer()) {
                ParagraphSystem system = new ParagraphSystem(resource);
                for (Paragraph paragraph : system.paragraphs()) {
                    if (paragraph.getType() == Paragraph.Type.NORMAL) {
                        Map<String, Object> result = new LinkedHashMap<String, Object>();
                        result.put("path", paragraph.getPath());
                        String excerpt = render(paragraph.getPath(), request, response);
                        result.put("excerpt", excerpt);
                        results.add(result);
                    }
                }
            }
        }

        return results;
    }

    protected String render(String path, SlingHttpServletRequest request, SlingHttpServletResponse response) {
        try {
            final Writer buffer = new StringWriter();
            final ServletOutputStream stream = new ServletOutputStream() {
                @Override
                public void write(int b) throws IOException {
                    // TODO: Proper character encoding support!
                    buffer.append((char) b);
                }
            };

            SlingHttpServletResponseWrapper wrapper =
                    new SlingHttpServletResponseWrapper(response) {
                        @Override
                        public ServletOutputStream getOutputStream() {
                            return stream;
                        }
                        @Override
                        public PrintWriter getWriter() throws IOException {
                            return new PrintWriter(buffer);
                        }
                        @Override
                        public SlingHttpServletResponse getSlingResponse() {
                            return super.getSlingResponse();
                        }
                    };

            // TODO: The ".html" suffix is a somewhat strange workaround
            // and should be removed. See SLING-633 for background.
            RequestDispatcher dispatcher =
                    request.getRequestDispatcher(path + ".html");
            dispatcher.include(request, wrapper);
            return buffer.toString();
        } catch (Exception e) {
            log.error("Exception occured: " + e.getMessage(), e);
            return e.getMessage();
        }
    }

    protected JSONObject generateJsonOutput(List<Map> paragraphs) throws JSONException {
        JSONArray hits = new JSONArray();

        for (Map paragraph : paragraphs) {
            JSONObject hit = new JSONObject();
            Iterator<String> keys = paragraph.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                Object val = paragraph.get(key);
                if ("excerpt".equals(key)) {
                    val = xssAPI.filterHTML((String)val);
                }
                hit.put(key, val);
            }
            hits.put(hit);
        }

        JSONObject json = new JSONObject().put("hits", hits).put("results", hits.length());

        return json;
    }

    protected String generateHtmlOutput(List<Map> paragraphs,
                                        SlingHttpServletRequest request,
                                        SlingHttpServletResponse response) throws ServletException, IOException {
        final XSSAPI xssapi = request.adaptTo(XSSAPI.class);
        if (request.getParameter("itemResourceType") == null) {
            // No resource type provided, concat the excerpts
            String html = "";
            for (Map paragraph : paragraphs) {
                html += paragraph.get("excerpt");
            }
            return xssapi.filterHTML(html);
        }

        StringResponseWrapper paragraphResponse = new StringResponseWrapper(response);
        RequestDispatcherOptions requestDispatcherOptions = new RequestDispatcherOptions(null);
        requestDispatcherOptions.setForceResourceType(request.getParameter("itemResourceType"));

        for (Map paragraph : paragraphs) {
            String path = (String) paragraph.get("path");
            if (path != null) {
                Resource resource = request.getResourceResolver().getResource(path);
                if (resource != null) {
                    request.setAttribute(Resource.class.getCanonicalName(), resource);
                    request.setAttribute("paragraphExcerpt", paragraph.get("excerpt"));
                    RequestDispatcher dispatcher = request.getRequestDispatcher(resource.getPath(),
                            requestDispatcherOptions);
                    dispatcher.include(request, paragraphResponse);
                    request.removeAttribute(Resource.class.getCanonicalName());
                    request.removeAttribute("paragraphExcerpt");
                }
            }
        }

        return xssapi.filterHTML(paragraphResponse.getString());
    }
}
