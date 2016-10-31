<%@include file="/libs/foundation/global.jsp"%>
<!-- need for POST method or else get 403 error -->
<cq:includeClientLib js="granite.csrf.standalone"/>
<%
    String baseURL = currentNode.getPath()+".json";
%>
<cq:includeClientLib categories="save-clientlib" />

<!-- Trigger the modal with a button -->
<button type="button" class="btn btn-warning" data-toggle="modal" data-target="#loginModal">Save</button>
<!-- Modal -->
<div id="loginModal" class="modal fade" role="dialog">
  <div class="modal-dialog">

    <!-- Modal content-->
    <div class="modal-content">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal">&times;</button>
        <h4 class="modal-title">Confirm Password</h4>
      </div>
      <div class="modal-body">
      	<form action ="<%= baseURL %>" method="POST">
        	<div class="form-group">
            	<label for="exampleInputPassword1">Username</label>
            		<input type="text" name="username" class="form-control" max-length="50" id="username">
            </div>
             <div class="form-group">
                    <label for="exampleInputPassword1">Password</label>
            			<input type="password" name="password" class="form-control" max-length="50" id="password">
            </div>
            <input type="hidden" name="cartitems" id="cartitems">
            <input type="hidden" name="cartprices" id="cartprices">
            <input type="hidden" name="cartqnts" id="cartqnts">
            <input type="hidden" name="cartproductids" id="cartproductids">
            <button value="submit" id="submit" class="btn btn-primary"> Submit</button>
      	</form>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
      </div>
    </div>

  </div>
</div>