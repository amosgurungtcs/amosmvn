<%@include file="/libs/foundation/global.jsp"%>
<%@page import = "com.day.text.Text, com.day.cq.wcm.foundation.Image, com.day.cq.commons.Doctype" %>
<%
    if (resource==null) {
    %>Error: Resource Null<%
} else {
    Image img = new Image(resource);
    img.setItemName(Image.NN_FILE, "file");
    img.setItemName(Image.PN_REFERENCE, "fileReference");
    img.setSelector("img");
    img.setDoctype(Doctype.fromRequest(request));
    img.setAlt("Home Page Placeholder");
    img.addAttribute("class","img_component");
    img.draw(out);
	}
%>