; This is the configuration file for i2b2
; Comments start with ';', as in php.ini

;Audit Configuration
audit.oracle.user = FRTHR_FQE
audit.oracle.password = @FURTHER_FQE@
;audit.oracle.connectionString = //host.does.not.exist:1521/orcl.sid
audit.oracle.connectionString = "(DESCRIPTION=(ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = @DB_HOST@)(PORT = 1521)))(CONNECT_DATA=(SID=FURTHER)))"

;SSO Configuration
oracle.user = I2B2USER
oracle.password = @I2B2USER@
;oracle.connectionString = //host.does.not.exist:1521/orcl.sid
oracle.connectionString = "(DESCRIPTION=(ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = @DB_HOST@)(PORT = 1521)))(CONNECT_DATA=(SID=FURTHER)))"
cas.url = @CAS_HOST@
cas.enabled = true
sso.hmac.secret = @SSO_HMAC_SECRET@

;Data Export Configuration
csv.export.url = http://@ESB_HOST@:9000/fqe/rest/fqe/query/export/CSV

;CSRF Protection
nonce.secret=@NONCE_SECRET@

demo = false
