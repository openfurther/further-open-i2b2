<?php

//
// phpCAS client
//

// import phpCAS lib
include_once('phpCAS/CAS.php');
// user-defined classes
include_once('dua.php');
include_once('userMapping.php');
include_once('audit.php');
require_once('cache.php');

nocache();

phpCAS::setDebug();

//Get the config from an ini file
$ini = parse_ini_file("includes/i2b2config.ini.php");

// initialize phpCAS
phpCAS::client(CAS_VERSION_2_0,$ini['cas.url'],443,'/cas');

// no SSL validation for the CAS server
phpCAS::setNoCasServerValidation();

//this is the single logout listener
phpCAS::handleLogoutRequests(true, array($_SERVER['HTTP_HOST']));

//The following code does not work locally, since 127.0.0.1 does not match localhost. But it should work on dev-i2b2-sw
//phpCAS::handleLogoutRequests();

// force CAS authentication
phpCAS::forceAuthentication();

// at this step, the user has been authenticated by the CAS server
// and the user's login name can be read with phpCAS::getUser().

// logout if desired
if (isset($_REQUEST['logout'])) {
	Audit::auditLogout($_REQUEST['userId']);
	phpCAS::logout();	
}
//get the LDAP user name (uNID)
$username=phpCAS::getUser();

if (!$ini['nonce.secret']) {
        echo 'Please configure nonce.secret to a random value within includes/i2b2config.ini.php';
        exit;
}

$now = time();

// Create a per session "password" for the user
$ticket = hash_hmac('sha256', $now . $username, $ini['sso.hmac.secret']);

session_start();
$_SESSION['time'] = $now;
$_SESSION['userId'] = $username;
$_SESSION['password'] = "FURTHER{" . $ticket . '+' . $now . "}";
$_SESSION['nonce'] = hash_hmac('sha256', $now . $username, $ini['nonce.secret']);

//check if the user is a first time user
$consent=DataUseAgreement::isDataUseAgreementValid($username);

if(!$consent)//forward to a data use agreement web page
{
	header('Location: firstTimeUserDua.php');
	exit;
}

?>