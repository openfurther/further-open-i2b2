; This is the configuration file for i2b2
; Comments start with ';', as in php.ini

;Audit Configuration
audit.oracle.user = FRTHR_FQE
audit.oracle.password = password
audit.oracle.connectionString = //host.does.not.exist:1521/orcl.sid

;SSO Configuration
oracle.user = I2B2USER
oracle.password = password
oracle.connectionString = //host.does.not.exist:1521/orcl.sid
cas.url = host.does.not.exist

;Data Export Configuration
csv.export.url = https://host.does.not.exist:9005/fqe/rest/fqe/query/export/CSV