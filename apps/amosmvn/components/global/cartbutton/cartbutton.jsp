<%@include file="/libs/foundation/global.jsp"%>
<cq:includeClientLib categories="cartbutton" />

<%
    String link = currentStyle.get("link","#");
%>
<a href="<%=link%>.html">
    <button class="btn btn-primary vmargin" type="button">
        <span class="glyphicon glyphicon-shopping-cart"> </span> CART<span class="badge"><span id="cartvalue"></span></span>
    </button> <!-- /.button -->
</a>
