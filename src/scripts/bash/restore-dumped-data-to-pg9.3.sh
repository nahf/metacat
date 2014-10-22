#!/bin/sh

#This script is used to move the data of a postgresql 8.4 instance to the
#new installed postgresql 9.3 instance after the os was upgraded from ubuntu 10.04 to 14.04.
#Basically it will decompress a metacat backup file which contains a dumped-all file and
#resotre it in a postgresql 9.3 instance.
#You need to pass the metacat backup file to this script. We assume the backup file 
#locating at /var/metacat/metacat-backup directory.
#This script should be run as the root user. 
#Usage: nohup ./restore-dumped-data-to-pg9.3.sh metacat_backup_file_name &

#Check the argument of the script. It only can have one - the metacat backup file name.

METACAT_BACKUP_DIR=/var/metacat/metacat-backup
SQL_FILE=metacat-postgres-backup.sql
METACAT_BACKUP_FILE_SUFFIX=.tgz
DB_BASE=/var/lib/postgresql
OLD_DB_VERSION=8.4
ANOTHER_OLD_DB_VERSION=9.1
NEW_DB_VERSION=9.3
NEW_DB_CONFIG=/etc/postgresql/$NEW_DB_VERSION/main/postgresql.conf
OLD_DB_DATA_DIR=$DB_BASE/$OLD_DB_VERSION
OLD_DB_BACKUP_FILE=postgresql-$OLD_DB_VERSION.tar.gz
POSTGRES_USER=postgres
PORT=5432
echo "start to move database from $OLD_DB_VERSION to $NEW_DB_VERSION at"
echo `date`

echo "the length of argument is $#"
#echo $@
if [ $# -ne 1 ]; then
   echo "This script should take one and only one parameter as the metacat backup file name.";
   exit 1;
fi
METACAT_BACKUP_FILE_NAME=$1
echo "the backup file name is $METACAT_BACKUP_FILE_NAME"
DECOMPRESS_DIR_NAME=`echo "$METACAT_BACKUP_FILE_NAME" | cut -d'.' -f1`
echo "the decmporessed dir is $DECOMPRESS_DIR_NAME"

echo "back up the old db data at $OLD_DB_DATA_DIR"
su - $POSTGRES_USER -c "tar -zcvf $DB_BASE/$OLD_DB_BACKUP_FILE $OLD_DB_DATA_DIR"

echo "stop postgresql"
/etc/init.d/postgresql stop

echo "remove postgresql 8.4 and 9.1"
apt-get remove postgresql-$OLD_DB_VERSION
apt-get remove postgresql-$ANOTHER_OLD_DB_VERSION

echo "modify the port to 5432 in the new db configuration file"
sed -i.bak --regexp-extended "s/(port =).*/\1${PORT}/;" $NEW_DB_CONFIG

echo "delete the data directory of 8.4"
rm -rf $OLD_DB_DATA_DIR

echo "decompress the metacat backup file"
tar zxvf $METACAT_BACKUP_DIR/$METACAT_BACKUP_FILE_NAM -C $METACAT_BACKUP_DIR

echo "restore database"
su - POSTGRES_USER -c "psql -f $METACAT_BACKUP_DIR/$DECOMPRESS_DIR_NAME/$SQL_FILE postgres"

echo "end to move database from $OLD_DB_VERSION to $NEW_DB_VERSION at"
echo `date`
