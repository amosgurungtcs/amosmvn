<%@include file="/libs/foundation/global.jsp"%>
<% 
String img1 = properties.get("image1", "deault");
String img2 = properties.get("image2", "deault");
String img3 = properties.get("image3", "deault");
String img4 = properties.get("image4", "deault");
String img5 = properties.get("image5", "deault");
String img6 = properties.get("image6", "deault");

%>
<!--------------------------- Carousel ------------------------------>
	<div class="recommended">
		<div id="mobileCarousel" class="carousel slide" data-ride="carousel">
			<!-- Wrapper for slides -->
			<div class="carousel-inner" role="listbox">
				<div class="item active">
					<div class="row">
						<div class="col-xs-4">
							<center><img src="<%= img1 %>" alt="mobile phone"></center>
						</div>
						<div class="col-xs-4">
							<center><img src="<%= img2 %>" alt="mobile phone"></center>
						</div>
						<div class="col-xs-4">
							<center><img src="<%= img3 %>" alt="mobile phone"></center>
						</div>
					</div>
				</div>

				<div class="item">
					<div class="row">
						<div class="col-xs-4">
							<center><img src="<%= img4 %>" alt="mobile phone"></center>
						</div>
						<div class="col-xs-4">
							<center><img src="<%= img5 %>" alt="mobile phone"></center>
						</div>
						<div class="col-xs-4">
							<center><img src="<%= img6 %>" alt="mobile phone"></center>
						</div>
					</div>
				</div>

			<!-- Left and right controls -->
			<a class="left carousel-control" href="#mobileCarousel" role="button"
				data-slide="prev"> <span class="glyphicon glyphicon-chevron-left"
				aria-hidden="true"></span> <span class="sr-only">Previous</span>
			</a> <a class="right carousel-control" href="#mobileCarousel" role="button"
				data-slide="next"> <span
				class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span>
				<span class="sr-only">Next</span>
			</a>
		</div>
	</div>