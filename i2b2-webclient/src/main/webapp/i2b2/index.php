<?php

/****************************************************************

  PHP-BASED I2B2 PROXY "CELL"

	Author: Nick Benik
	Last Revised: 9-15-08

*****************************************************************

This file acts as a simple i2b2 proxy cell.  If no variables have been sent it is assumed that the request is from a
user's Web browser requesting the default page for the current directory.  In this case, this file will read the
contents of the default.htm file and return its contents to the browser via the current HTTP connection.

*/

//Get the config from an ini file
$ini = parse_ini_file("includes/i2b2config.ini.php");

if ($ini['cas.enabled']) {
	require_once('includes/sso.php');
}
require_once('includes/cache.php');

nocache();

$WHITELIST = array(
	"http://",
	"http://127.0.0.1:9090/axis2/rest/",
	"http://localhost:9090/axis2/rest/",
	"http://127.0.0.1:7070/i2b2/rest/",
	"http://localhost:7070/i2b2/rest/",
	"http://webservices.i2b2.org",
	"https://webservices.i2b2.org",
	"http://services.i2b2.org",
	"https://services.i2b2.org"
);



$BLACKLIST = array(
	"http://127.0.0.1:9090/test",
	"http://localhost:9090/test",
	"http://127.0.0.1:7070/test",
	"http://localhost:7070/test"
);



$PostBody = file_get_contents("php://input");
if ($PostBody=="") {
	require_once('default.php');
} else {
	// Process the POST for proxy redirection

	// Validate that POST data is XML and extract <proxy> tag
	$xmlPost = new SimpleXMLElement($PostBody);
	if (!$xmlPost) { die("The POST body was not valid XML!"); }
	$proxyNodeMatches = $xmlPost->xpath('message_header/proxy[redirect_url]');
	if (count($proxyNodeMatches) == 0 ) { die("No valid Proxy Redirect nodes were found in the XML message!"); }
	// only deal with the first definition
	$proxyMsg = $proxyNodeMatches[0];
	$proxyURL = (string)$proxyMsg->redirect_url;
	$proxyXML = $proxyMsg->asXML();

	// regenerate the XML string but filter out the proxy node we are using
	$newXML = $xmlPost->asXML();
	$newXML = str_replace($proxyXML, '', $newXML);

	// ---------------------------------------------------
	//   white-list processing on the URL
	// ---------------------------------------------------
	$isAllowed = false;
	$requestedURL = strtoupper($proxyURL);
	foreach ($WHITELIST as $entryValue) {
		$checkValue = strtoupper(substr($requestedURL, 0, strlen($entryValue)));
		if ($checkValue == strtoupper($entryValue)) {
			$isAllowed = true;
			break;
		}
	}
	if (!$isAllowed) {
		// security as failed - exit here and don't allow one more line of execution the opportunity to reverse this
		die("The proxy has refused to relay your request.");
	}
	// ---------------------------------------------------
	//   black-list processing on the URL
	// ---------------------------------------------------
	foreach ($BLACKLIST as $entryValue) {
		$checkValue = strtoupper(substr($requestedURL, 0, strlen($entryValue)));
		if ($checkValue == strtoupper($entryValue)) {
			// security as failed - exit here and don't allow one more line of execution the opportunity to reverse this
			die("The proxy has refused to relay your request.");
		}
	}



	// open the URL and forward the new XML in the POST body
	$proxyRequest = curl_init($proxyURL);
	
	$bodyMatches = $xmlPost->xpath('message_body');

	// these options are set for hyper-vigilance purposes
	curl_setopt($proxyRequest, CURLOPT_COOKIESESSION, 0);
	curl_setopt($proxyRequest, CURLOPT_FORBID_REUSE, 1);
	curl_setopt($proxyRequest, CURLOPT_FRESH_CONNECT, 0);
	// Specify NIC to use for outgoing connection, fixes firewall+DMZ headaches
	// curl_setopt($proxyRequest, CURLOPT_INTERFACE, "XXX.XXX.XXX.XXX");
	// other options
	curl_setopt($proxyRequest, CURLOPT_RETURNTRANSFER, 1);
	curl_setopt($proxyRequest, CURLOPT_CONNECTTIMEOUT, 900); 	// wait 15 minutes
	// data to proxy thru only if there is a message body.
	if (count($bodyMatches) != 0 ) {
		curl_setopt($proxyRequest, CURLOPT_POST, 1);
		curl_setopt($proxyRequest, CURLOPT_POSTFIELDS, $newXML);
	}
	// SEND REQUEST!!!
	curl_setopt($proxyRequest, CURLOPT_HTTPHEADER, array('Expect:', 'Content-Type: text/xml'));
	$proxyResult = curl_exec($proxyRequest);
	// cleanup cURL connection
	curl_close($proxyRequest);

	// perform any analysis or processing on the returned result here
	header("Content-Type: text/xml", true);
	print($proxyResult);
}


?>

