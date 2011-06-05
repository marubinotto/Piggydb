#!/bin/sh

java -Dpiggydb.database.prefix=file:~/piggydb -Dpiggydb.database.name=piggydb -Dpiggydb.enableAnonymous=false -jar winstone.jar --warfile=@WAR_FILE_NAME@ $1 $2 $3 $4 $5
