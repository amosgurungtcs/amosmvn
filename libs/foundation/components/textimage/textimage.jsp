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

  Text-Image component

  Combines the text and the image component

--%><%@ page import="org.apache.sling.api.resource.ValueMap,
    com.day.cq.commons.DiffService,
    com.day.cq.commons.Doctype,
    com.day.cq.wcm.api.WCMMode,
    com.day.cq.wcm.api.components.DropTarget,
    com.day.cq.wcm.foundation.Image, com.day.cq.wcm.foundation.Placeholder" %><%
%><%@include file="/libs/foundation/global.jsp"%><%
    boolean isAuthoringUIModeTouch = Placeholder.isAuthoringUIModeTouch(slingRequest);

    Image image = new Image(resource, "image");

    // don't draw the placeholder in case UI mode touch it will be handled afterwards
    if (isAuthoringUIModeTouch) {
        image.setNoPlaceholder(true);
    }

    //drop target css class = dd prefix + name of the drop target in the edit config
    String ddClassName = DropTarget.CSS_CLASS_PREFIX + "image";

    // if diff is being taken, then also the image or default image has to be drawn
    String vLabel = request.getParameter(DiffService.REQUEST_PARAM_DIFF_TO);
    String placeholder = (isAuthoringUIModeTouch && !image.hasContent())
            ? Placeholder.getDefaultPlaceholder(slingRequest, component, "", ddClassName)
            : "";

    if (image.hasContent() || WCMMode.fromRequest(request) == WCMMode.EDIT || vLabel != null) {
        image.loadStyleData(currentStyle);
        // add design information if not default (i.e. for reference paras)
        if (!currentDesign.equals(resourceDesign)) {
            image.setSuffix(currentDesign.getId());
        }
        image.addCssClass(ddClassName);
        image.setSelector(".img");
        image.setDoctype(Doctype.fromRequest(request));

        String divId = "cq-textimage-jsp-" + resource.getPath();
        String imageHeight = image.get(image.getItemName(Image.PN_HEIGHT));
        // div around image for additional formatting
%><div class="image" id="<%= xssAPI.encodeForHTMLAttr(divId) %>"><%
%><% image.draw(out); %><%

    %><cq:text property="image/jcr:description" placeholder="" tagName="small" escapeXml="true"/>
    <cq:text escapeXml="true" placeholder="<%= placeholder %>"/>
</div><div class="clear"></div>
<%@include file="/libs/foundation/components/image/tracking-js.jsp"%><%
    }

    boolean showComponentName = false;
    Node node = resource.adaptTo(Node.class);
    if (properties.get("text",null) == null ){
        showComponentName = true;
        if(node.hasNode("image")) {
            Node imageNode=node.getNode("image");
            if(imageNode.hasProperty("fileReference")) {
                showComponentName = false;
            }
        }
        String showAltComponentName = request.getParameter("showAltComponentName");
        if(showAltComponentName!=null && showAltComponentName.equalsIgnoreCase("true") && showComponentName) { %>
            <h2 class="cq-paragraphreference-thumbnail-text"><%= resource.getName() %></h2>
        <%}
    }%>

<cq:text property="text" tagClass="<%= "text " + ddClassName %>" escapeXml="true" placeholder="<%= placeholder %>"/><div
        class="clear"></div>
