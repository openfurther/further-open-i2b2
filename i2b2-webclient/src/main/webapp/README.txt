The i2b2 directory is the i2b2 webclient source code that should be deployed to the i2b2 VM
(155.100.160.90) under /var/www/html. It contains the FURTHeR tweaks to the original i2b2
web client code.

Note: because the YUI source code directory is relatively large (~14M), it is NOT included
in the svn distribution and assumed to exist under /var/www/html/i2b2/js-ext. If you
are setting up a new working copy of the FURTHeR i2b2 web client on an i2b2 server, make
sure to copy the js-ext directory from the original /var/www/html/i2b2 directory. It is
also a good idea to back up the original i2b2 web client dir before overridding it with
the FURTHeR svn version.