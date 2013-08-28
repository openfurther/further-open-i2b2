<?php


class UserMapping {


        function setPassword($username, $value){

                $ini = parse_ini_file("i2b2config.ini.php");
                $conn = oci_connect($ini['oracle.user'], $ini['oracle.password'], $ini['oracle.connectionString']);
                if (!$conn) {
                        $error = oci_error();
                        echo "Oracle connection error: ".$error['message'];
                }

                $sql = "UPDATE gspassword SET value=:value,datelastmodified=SYSDATE WHERE sportletuser = (SELECT gsoid FROM sportletuserimpl WHERE userid = :userId)";

                $stid = oci_parse($conn, $sql);
                oci_bind_by_name($stid, ":value", md5($value));
                oci_bind_by_name($stid, ":userId", $username);

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

                oci_free_statement($stid);
                oci_close($conn);

        }

}


?>
