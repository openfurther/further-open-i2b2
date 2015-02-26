<?php 
/*
 *  Check if the user is a first-time user. If yes, show the first-time user data use agreement screen, require the user to sign it
 *  and log the signing event. 
 */

include_once('audit.php');

//for testing purpose
// $ldapUserName = "u0031312"

class DataUseAgreement{
	
// 	define("AUDIT_TABLE_NAME", "COM_AUDIT_TRAIL");
// 	define("DATA_USE_AGREEMENT_EVENT", "DATA_USE_AGREEMENT_CONSENT_SUCCESS");
	
	const DATA_USE_AGREEMENT_EVENT = "DATA_USE_AGREEMENT_CONSENT_SUCCESS";
	
	
	
	function isDataUseAgreementValid($userId){
		
		$conn= Audit::getConnection();
		
		// Prepare the statement
		$sql="select COUNT(*) from ".Audit::AUDIT_TABLE_NAME." where aud_user= :userId and aud_action= :event";
		
		$stid = oci_parse($conn, $sql);
		
		$event=self::DATA_USE_AGREEMENT_EVENT;
		oci_bind_by_name($stid, ":userId", $userId);
		oci_bind_by_name($stid, ":event", $event);
		
		if (!$stid) {
			$error = oci_error($conn);
			echo "Oracle SQL parsing error: ".$error['message'];
		}
		
		// Perform the logic of the query
		$result = oci_execute($stid);
		if (!$result) {
			$error = oci_error($stid);
			echo "Oracle SQL execution error: ".$error['message'];
		}
		
		$ret = false;
		// Fetch the results of the query
		$row = oci_fetch_array($stid, OCI_NUM);
		if ($row[0]>0)//The user has a valid data use agreement consent
			$ret = true;
		else//first time user
			$ret = false;
		
		oci_free_statement($stid);
		oci_close($conn);
		
		return $ret;
	}
	
	function consentDataUseAgreement($userId){
		
		$conn= Audit::getConnection();
		
		$stid = oci_parse($conn,"INSERT INTO ".Audit::AUDIT_TABLE_NAME." (ID, AUD_USER, AUD_ACTION, AUD_RESOURCE, AUD_DATE) VALUES(:id, :user_bv, :action_bv, :resource_bv, SYSDATE)");
		
		$event=self::DATA_USE_AGREEMENT_EVENT;
		$resource=Audit::AUDIT_RESOURCE;
		
		oci_bind_by_name($stid,":id",Audit::nextAuditId());
		oci_bind_by_name($stid,":user_bv",$userId);
		oci_bind_by_name($stid,":action_bv",$event);
		oci_bind_by_name($stid,":resource_bv",$resource);
		
		$ret = false;
		$result = oci_execute($stid);
		
		if(!$result){
			$error = oci_error($stid);
			echo "Oracle SQL execution error: ".$error['message'];
			$ret = false;
		} else {
			$ret = true;
		}
		
		oci_free_statement($stid);
		oci_close($conn);
		
		return $ret;

	}
	
}
?>
