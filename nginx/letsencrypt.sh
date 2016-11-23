#!/bin/bash

set -e

pushd /opt/letsencrypt

./letsencrypt-auto certonly --webroot --email niloc132@gmail.com -d viola.colinalworth.com --webroot-path /var/www/letsencrypt/
./letsencrypt-auto certonly --webroot --email niloc132@gmail.com -d static.viola.colinalworth.com --webroot-path /var/www/letsencrypt/

popd