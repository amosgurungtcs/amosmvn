<%@page session="false"%><%--
  Copyright 1997-2008 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  ==============================================================================

  Scaffolding selector script

  Finds and includes the correct scaffold for the currentPage.

--%><%@page contentType="text/html" pageEncoding="utf-8" import="
        javax.jcr.Node,
        com.adobe.granite.ui.components.Value,
        org.apache.sling.api.resource.Resource,
        com.day.cq.wcm.api.components.IncludeOptions,
        com.day.cq.wcm.core.utils.ScaffoldingUtils,
        java.io.UnsupportedEncodingException,
        java.net.URLEncoder" %><%
%><%@taglib prefix="cq" uri="http://www.day.com/taglibs/cq/1.0" %><%
%><%@taglib prefix="sling" uri="http://sling.apache.org/taglibs/sling/1.0" %><%
%><cq:defineObjects/><%

    // first check if the page has a scaffold specified
    String scaffoldPath = pageProperties.get("cq:scaffolding", "");
    if (scaffoldPath.length() == 0) {
        // search all scaffolds for the correct template
        // this should be improved and respect template + best content path
        Resource scRoot = resourceResolver.getResource("/etc/scaffolding");
        Node root = scRoot == null ? null : scRoot.adaptTo(Node.class);
        if (root != null) {
            scaffoldPath = ScaffoldingUtils.findScaffoldByTemplate(root, pageProperties.get("cq:template", ""));
            if (scaffoldPath == null) {
                scaffoldPath = ScaffoldingUtils.findScaffoldByPath(root, currentPage.getPath());
            }
        }
    }
    if (scaffoldPath == null || scaffoldPath.length() == 0) {
        // use default
        scaffoldPath = "/etc/scaffolding";
    }
    scaffoldPath += "/jcr:content";
    if (resourceResolver.getResource(scaffoldPath + "/cq:dialog") != null) {
        try {
            request.setAttribute(Value.CONTENTPATH_ATTRIBUTE, currentPage.getPath());
            %><sling:include path="<%= "/libs/wcm/core/content/editor/scaffoldhost.html/" + scaffoldPath %>" /><%
        } finally {
            request.removeAttribute(Value.CONTENTPATH_ATTRIBUTE);
        }
    } else {
        // fall back to classic scaffolding
        IncludeOptions.getOptions(request, true).forceSameContext(true);
        %><cq:include path="<%= scaffoldPath + ".html" %>" resourceType="wcm/scaffolding/components/scaffolding" /><%
    }

%>

