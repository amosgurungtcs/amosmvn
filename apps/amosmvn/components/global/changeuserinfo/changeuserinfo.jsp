<%@include file="/libs/foundation/global.jsp"%>
<h3>Change User Information:</h3>
<form>
	<b>Username:</b><br />
	<input type="text" class="form-control" id="username" placeholder="user" maxlength="50">
	<b>Name:</b><br />
	<input type="text" class="form-control" id="name" placeholder="John Doe" maxlength="50">
	<b>Email:</b><br />
	<input type="email" class="form-control" id="email" placeholder="johndoe@email.com" maxlength="50">
	<br />
	<input type = "submit" class="btn btn-info" value="Update Profile">
</form>