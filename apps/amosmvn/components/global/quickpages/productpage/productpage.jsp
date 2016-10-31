<%@include file="/libs/foundation/global.jsp"%>
<%
    String productname = properties.get("productname","Product Name");
	String productlink = properties.get("productlink","#");
	String productdescription = properties.get("productdescription", "Product Description");

	int price = properties.get("price",0);
    %>
<div class="row">
	<!--<div class="col-xs-0 col-sm-2">
    </div>-->
	<div class="col-xs-12 col-sm-4">
        <a href="<%= productlink %>.html "><cq:include path="product_img" resourceType="Amos/components/global/image" /></a>
    </div>
	<div class="col-xs-6 col-sm-4">
        <a href="<%= productlink %>.html "><b><%=productname%></b></a>
        <br />
        <%= productdescription %>
    </div>
    <div class="col-xs-6 col-sm-4">
        <b>$<%= price %></b>
    </div>
</div>
<br />