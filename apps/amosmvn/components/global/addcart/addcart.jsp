<%@include file="/libs/foundation/global.jsp"%>
<cq:includeClientLib categories="addcart" />
<%
	int productid = properties.get("productid",0);
    %>

<div align="right">
    <form>
        <b>Quantity:</b>
      	<input type="number" name="qnt" id="qnt" min="1" value="1" max="100">
        <input type="hidden" name="productid" id="productid" value="<%=productid%>">
    </form>
    <br />
    <button class="btn btn-primary" id="addcart" type="button">
        Add to CART
    </button> <!-- /.button -->
</div>