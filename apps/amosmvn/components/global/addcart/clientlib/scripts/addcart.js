$(document).ready(function(){
    $("#addcart").click(function(){
        var cartcount = sessionStorage.getItem("cartcount");

        //increment cartcount
        if(cartcount==null){
            cartcount = 0;
        }
        var intcartcount =  parseInt(cartcount);
        intcartcount +=1;
        sessionStorage.setItem("cartcount",intcartcount);

        //get values of product
        var item = $( "#productname" ).text();
        var price = $( "#price" ).text();
        var qnt = $('#qnt').val();
        var productid = $('#productid').val();

        //get current cart values from sessionStorage
        var cartitems = sessionStorage.getItem("cartitems");
        var cartprices = sessionStorage.getItem("cartprices");
		var cartqnts = sessionStorage.getItem("cartqnts");
        var cartproductids = sessionStorage.getItem("cartproductids");

        if(cartitems!=null){
        	cartitems += ';';
            cartitems +=item;
            cartprices += ';';
            cartprices +=price;
            cartqnts += ';';
            cartqnts +=qnt;
            cartproductids +=';';
            cartproductids +=productid;
        }else{
			cartitems = item;
            cartprices =price;
            cartqnts = qnt;
            cartproductids = productid;
        }
        sessionStorage.setItem("cartitems",cartitems);
        sessionStorage.setItem("cartprices",cartprices);
        sessionStorage.setItem("cartqnts",cartqnts);
        sessionStorage.setItem("cartproductids",cartproductids);
        location.reload();
    });
});