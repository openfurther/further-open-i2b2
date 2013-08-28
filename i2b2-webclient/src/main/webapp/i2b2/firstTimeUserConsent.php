<?php

require_once('includes/cache.php');
nocache();

//Prevent invalid call to this php script 
$refering=parse_url($_SERVER['HTTP_REFERER']);

$ini = parse_ini_file("includes/i2b2config.ini.php");

session_start();
$nonce = hash_hmac('sha256', $_SESSION['time'] . $_SESSION['userId'], $ini['nonce.secret']);

if(($refering['host']==$_SERVER['HTTP_HOST']) && $_SESSION['nonce'] === $nonce){
	
	require_once('includes/dua.php');
	require_once('includes/userMapping.php');
	
	//This page should not be invoked if the user already signed the DUA.		
	if(DataUseAgreement::isDataUseAgreementValid($_SESSION['userId']))
		echo "Invalid request";
	else{
			if($_POST['agree'] == 'Yes')
			{
				DataUseAgreement::consentDataUseAgreement($_SESSION['userId']);
				header('Location: default.php');
				
			}
			else
			{
				echo "Sorry you can not use the system if you don't sign the data use agreement. If you have any question, please contact us at further@utah.edu.";
			}
	
	}
}else{
	echo "Invalid request";
}
?>