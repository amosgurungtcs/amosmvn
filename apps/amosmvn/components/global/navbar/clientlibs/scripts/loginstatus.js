$(document).ready(function(){
	document.getElementById("user").innerHTML = sessionStorage.getItem("username");
    var logedin = document.getElementById("logedin");
    var notlogedin =document.getElementById("notlogedin");
    logedin.style.visibility = 'visible';
    logedin.style.visibility = 'visible';
    if(sessionStorage.getItem("username")==null)
    {
		logedin.style.display = 'none';
    }else{
		notlogedin.style.display = 'none';
    }
});
