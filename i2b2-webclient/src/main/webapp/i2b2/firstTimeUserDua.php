<?php
require_once('includes/cache.php');
nocache();
session_start();
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Data Use Agreement</title>

<link rel="stylesheet" type="text/css" href="help/assets/help.css" />

</head>
<body>
	<h1>First-time User</h1>
	<p>
	We noticed that you are for the first time to access FURTHeR. An explict data use agreement is required. Please check the box below to continue. This page won't show up again next time you login.		
	</p>
	
	<?php include("duaContent.html");?>


<form action="firstTimeUserConsent.php" method="post">
	<input type="hidden" name="userId" value="<?php echo $_SESSION['userId']; ?>"/>
    <input type="checkbox" name="agree" value="Yes" /> I agree with the above items
    <input type="submit" name="submit" value="Submit" />
</form>


	
</body>
</html>