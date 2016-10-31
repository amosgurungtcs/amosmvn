<%@include file="/libs/foundation/global.jsp"%>
<%
    String productname = properties.get("productname","Product Name");
	String productdescription = properties.get("productdescription", "Product Description");

	int price = properties.get("price",0);
    %>
<h3><b><a href="#" id="productname"><%=productname%></a></b></h3>
<h3>$<b id="price"><%=price%></b></h3>
<p><%=productdescription %></p>