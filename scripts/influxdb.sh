#!/usr/bin/env bash
wget https://dl.influxdata.com/influxdb/releases/influxdb_${INFLUXDB_VERSION}_amd64.deb
sudo dpkg -i influxdb_${INFLUXDB_VERSION}_amd64.deb
sudo mv $TRAVIS_BUILD_DIR/src/test/resources/influxdb.conf /etc/influxdb/influxdb.conf
sudo service influxdb start
/usr/bin/influx --execute "CREATE USER admin WITH PASSWORD 'admin' WITH ALL PRIVILEGES"