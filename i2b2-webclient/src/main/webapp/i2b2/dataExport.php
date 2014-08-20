<?php

//Prevent invalid call to this php script 
$refering=parse_url($_SERVER['HTTP_REFERER']);

if($refering['host']==$_SERVER['HTTP_HOST']){
	
	//Get the config from an ini file
	$ini = parse_ini_file("includes/i2b2config.ini.php");
	
	$exportContext = $_POST['exportContext'];
	
	$postData= array(
		'exportContext' => $exportContext
	);
	$ch = curl_init($ini['csv.export.url']);
	
	//not verify the CA certificate for now to make curl work with https
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
	curl_setopt($ch,CURLOPT_POST, true);
	curl_setopt($ch,CURLOPT_POSTFIELDS, http_build_query($postData));
	curl_setopt($ch, CURLOPT_HEADER, true);
	curl_setopt($ch, CURLOPT_BINARYTRANSFER, true);
	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
	
	
	$response = curl_exec($ch);
	$header_size = curl_getinfo($ch, CURLINFO_HEADER_SIZE);

	curl_close($ch);
	
	//var_dump($response);
	$header = substr($response, 0, $header_size);
	$body = substr($response, $header_size);
	
	//The following code does not work on linux but work on windows
	//list ($headerString, $body) = explode(''.PHP_EOL.''.PHP_EOL, $response, 2);


	if(strstr($body, '<?xml')){
		$retObject=new SimpleXMLElement($body);
                // error code/message has been coming back qualified 
                // with ns2, so check for this and use it to navigate xml 
                $namespaces = $retObject->getNameSpaces(true);
                $ns2 = $retObject->children($namespaces['ns2']);
                if(is_null($ns2)){
                        $errorMsg = $retObject->message;
                } else {
                        $errorMsg = $ns2->message;
                }
		//echo "<script type='text/javascript'>window.alert('".$errorMsg."')</script>";
		echo "<script type='text/javascript'>window.alert('".$errorMsg."'); window.location ='https://".$_SERVER['HTTP_HOST']."/i2b2';</script>";
		
		exit;
	}
	
	else{
	
		$today = date("m-d-Y-H-i-s");
		header("Content-type: text/csv");
		header("Content-Disposition: attachment; filename=further-export-".$today.".csv");
		header("Pragma: no-cache");
		header("Expires: 0");
		echo $body;
	}	

}else{
	echo "Invalid request";
}


	



?>