<%@include file="/libs/foundation/global.jsp"%>
<form action ="/content/Amos/search.html" >
		    <div class="container"><div class="row">

			    <div class ="col-sm-2 col-xs-6 text-center" >
                    <span align="center"> <cq:include path="headerlogo-comp" resourceType="Amos/components/global/logo" /></span>
			    </div>
                <div class="col-sm-2 col-xs-6 col-sm-push-8 text-center">
                  <center>
                      <cq:include path="cart_node" resourceType="Amos/components/global/cartbutton" />
                  </center>
		      </div><!-- /.col-xs-2 -->
		      <div class="col-sm-8 col-xs-12 col-sm-pull-2 text-center">
		        <span class="input-group input-group-lg">
	   					<input name="q" class="form-control"/>
						<span class="input-group-btn">
		            		<button type="submit" id="submitsearch" class="btn">Search</button>
		          		</span><!-- /btn-group -->                   

		        </span><!-- /input-group -->
		      </div><!-- /.col-xs-8 -->
		    </div></div><!-- /.row -->
</form>
