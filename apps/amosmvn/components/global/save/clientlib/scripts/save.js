
$(document).ready(function(){
    var username = sessionStorage.getItem("username");
    var cartitems = sessionStorage.getItem("cartitems");
    var cartprices = sessionStorage.getItem("cartprices");
    var cartqnts = sessionStorage.getItem("cartqnts");
    var cartproductids = sessionStorage.getItem("cartproductids");

    $('#username').val(username);
	$('#cartitems').val(cartitems);
	$('#cartprices').val(cartprices);
    $('#cartqnts').val(cartqnts);
    $('#cartproductids').val(cartproductids);

    $("#save").click(function(){
		alert("saved");
    });
});
