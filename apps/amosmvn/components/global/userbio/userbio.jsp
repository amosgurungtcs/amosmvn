<%@include file="/libs/foundation/global.jsp"%>

<%
    String biotitle = properties.get("biotitle", "User Bio:");
	String biodefault = properties.get("biodefault", "BIO text");
   %>
<div class="alert alert-info">
	<h2><%= biotitle %></h2>
	<p>
        <%= biodefault %>
    </p>
</div>