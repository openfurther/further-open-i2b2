<?php 


class Audit{
	
	const AUDIT_TABLE_NAME = "COM_AUDIT_TRAIL";
	const AUDIT_RESOURCE = "i2b2 web client";
	const LOGIN_EVENT = "LOGIN_SUCCESS";
	const LOGOUT_EVENT = "LOGOUT_SUCCESS";
	
	
	
	/**
	 *
	 * @return an oracle db connection identifier
	 */
	
	function getConnection(){
		//Get the connection parameters from an ini file
		$ini = parse_ini_file("i2b2config.ini.php");
	
		//Establish a connection to the Oracle database. Returns a connection identifier or FALSE on error
		$conn = oci_connect($ini['audit.oracle.user'], $ini['audit.oracle.password'], $ini['audit.oracle.connectionString']);
		if (!$conn) {
			$error = oci_error();
			echo "Oracle connection error: ".$error['message'];
		}
		return $conn;
	}
	
	
	function auditLogin($userId){
		$conn= self::getConnection();
		
		$stid = oci_parse($conn,"INSERT INTO ".self::AUDIT_TABLE_NAME." (ID, AUD_USER, AUD_ACTION, AUD_RESOURCE, AUD_DATE) VALUES(:id, :user_bv, :action_bv, :resource_bv, SYSDATE)");
		
		$event=self::LOGIN_EVENT;
		$resource=self::AUDIT_RESOURCE;
		oci_bind_by_name($stid,":id",self::nextAuditId());
		oci_bind_by_name($stid,":user_bv",$userId);
		oci_bind_by_name($stid,":action_bv",$event);
		oci_bind_by_name($stid,":resource_bv",$resource);
		
		$result = oci_execute($stid);
		if(!$result){
			$error = oci_error($stid);
			echo "Oracle SQL execution error: ".$error['message'];
			return false;
		}
			
		oci_free_statement($stid);
		oci_close($conn);
		
		return true;
	}
	
	function auditLogout($userId){
		
		if(is_null($userId))
			$userId="";
		
		$conn= self::getConnection();
		
		$stid = oci_parse($conn,"INSERT INTO ".self::AUDIT_TABLE_NAME." (ID, AUD_USER, AUD_ACTION, AUD_RESOURCE, AUD_DATE) VALUES(:id, :user_bv, :action_bv, :resource_bv, SYSDATE)");
		
		$event=self::LOGOUT_EVENT;
		$resource=self::AUDIT_RESOURCE;
		oci_bind_by_name($stid,":id",self::nextAuditId());
		oci_bind_by_name($stid,":user_bv",$userId);
		oci_bind_by_name($stid,":action_bv",$event);
		oci_bind_by_name($stid,":resource_bv",$resource);		
		
		$result = oci_execute($stid);
		if(!result){
			$error = oci_error($stid);
			echo "Oracle SQL execution error: ".$error['message'];
			return false;
		}
			
		oci_free_statement($stid);
		oci_close($conn);
		
		return true;
		
	}
	
	function nextAuditId(){
		
		$conn= self::getConnection();
		
		$stid = oci_parse($conn, "SELECT CAS_LOG_ID_SEQ.nextval from dual");
		$result = oci_execute($stid);

		if(!$result) {
			$error = oci_error($stid);
			echo "Error retrieving next audit id.";
			oci_free_statement($stid);
			oci_close($conn);
			exit;
		}

		$id = oci_fetch_array($stid, OCI_NUM);
		
		oci_free_statement($stid);
		oci_close($conn);
		
		return $id[0];
		
	}
	
}

?>