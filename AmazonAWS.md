# Apache Webserver #

# Java & Tomcat #


# Mail server #


# My SQL #
http://stackoverflow.com/questions/10676753/reducing-memory-consumption-of-mysql-on-ubuntuaws-micro-instance
Try to Change this setting in mysql configuration file (my.cnf)
key\_buffer              = 8M
max\_connections         = 30 // Limit connections
query\_cache\_size        = 8M // try 4m if not enough
query\_cache\_limit       = 512K
thread\_stack            = 128K

# PhpMyAdmin #


# Swap File #
http://stackoverflow.com/questions/10676753/reducing-memory-consumption-of-mysql-on-ubuntuaws-micro-instance

Also the micro instance has no swap, that might be a problem..
SWAPFILE=/mnt/swapfile.swap
dd if=/dev/zero of=$SWAPFILE bs=1M count=512
mkswap $SWAPFILE
swapon $SWAPFILE

Then in /etc/rc.local add:
swapon /mnt/swapfile.swap