<%--
  Copyright 1997-2010 Day Management AG
  Barfuesserplatz 6, 4001 Basel, Switzerland
  All Rights Reserved.

  This software is the confidential and proprietary information of
  Day Management AG, ("Confidential Information"). You shall not
  disclose such Confidential Information and shall use it only in
  accordance with the terms of the license agreement you entered into
  with Day.

  Password Reset component.

  Reset a user's password

--%><%
%><%@include file="/libs/foundation/global.jsp"%><%
%><%@page session="false"
          import="java.util.Locale,
                  java.util.ResourceBundle,
                  org.apache.commons.lang.StringUtils,
                  com.day.cq.i18n.I18n,
                  com.day.cq.wcm.foundation.forms.FormsHelper,
                  com.day.cq.wcm.foundation.forms.LayoutHelper" %><%
%><%
    final Locale pageLocale = currentPage.getLanguage(true);
    final ResourceBundle resourceBundle = slingRequest.getResourceBundle(pageLocale);
    I18n i18n = new I18n(resourceBundle);

    final String name = FormsHelper.getParameterName(resource);
    final String id = FormsHelper.getFieldId(slingRequest, resource);
    final String currentPswdTitle = properties.get("currentPswdTitle", i18n.get("Current Password"));
    final boolean hideCurrentPswdTitle = properties.get("hideCurrentPswdTitle", false);
    final String title = FormsHelper.getTitle(resource, i18n.get("Password"));
    final boolean hideTitle = properties.get("hideTitle", false);
    final String confirmTitle = properties.get("confirmationTitle", i18n.get("Confirm Password"));
    final boolean hideConfirmTitle = properties.get("hideConfirmTitle", false);
    final boolean required = FormsHelper.isRequired(resource);

    String width = properties.get("width", "");
    if (width.length() > 0) {
        width = width + "px;";
    }
    final String cols = properties.get("cols", "35");

    final String key = request.getParameter("ky") == null ? null : slingRequest.getRequestParameter("ky").getString();
    final String uid = request.getParameter("uid") == null ? null : slingRequest.getRequestParameter("uid").getString();

    boolean tokenBasedReset = StringUtils.isNotEmpty(key);

    if (!tokenBasedReset) {

%><div class="form_row">
    <% LayoutHelper.printTitle(id, currentPswdTitle, required, hideCurrentPswdTitle, out); %>
    <div class="form_rightcol">
        <input class="geo textinput" name="currentPwd" value="" type="password" autocomplete="off" size="<%=xssAPI.encodeForHTMLAttr(cols)%>" <%= width.length() > 0 ? "style=\"width:" + xssAPI.encodeForHTMLAttr(width) + "\"" : "" %>>
    </div>
</div><%

    }

%><div class="form_row">
    <% LayoutHelper.printTitle(id, title, required, hideTitle, out); %>
    <div class="form_rightcol">
        <input class="geo textinput" name="<%=xssAPI.encodeForHTMLAttr(name)%>" value="" type="password" autocomplete="off" size="<%=xssAPI.encodeForHTMLAttr(cols)%>" <%= width.length() > 0 ? "style=\"width:" + xssAPI.encodeForHTMLAttr(width) + "\"" : "" %>>
    </div>
</div>
<div class="form_row">
    <% LayoutHelper.printTitle(id, confirmTitle, required, hideConfirmTitle, out); %>
    <div class="form_rightcol">
        <input class="geo textinput" name="<%=xssAPI.encodeForHTMLAttr(name)%>_confirm" value="" type="password" autocomplete="off" size="<%=xssAPI.encodeForHTMLAttr(cols)%>" <%= width.length() > 0 ? "style=\"width:" + xssAPI.encodeForHTMLAttr(width) + "\"" : "" %>>
    </div>
</div><%

    if (tokenBasedReset) {

%><input type="hidden" name="ky" value="<%=xssAPI.encodeForHTMLAttr(key)%>">
<input type="hidden" name="uid" value="<%=xssAPI.encodeForHTMLAttr(uid)%>"><%

    }

    LayoutHelper.printDescription(FormsHelper.getDescription(resource, ""), out);
    LayoutHelper.printErrors(slingRequest, name, out);

%>
