$(document).ready(function(){
	$("#logout").click(function(){
        $.get("/", function(){
            sessionStorage.clear();
			window.location.replace("/content/Amos/signon.html");
		});
	});
});