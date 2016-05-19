# force redirects to ssl
server {
        server_name viola.colinalworth.com static.viola.colinalworth.com;
        listen 80;
        return 301 https://$host$request_uri;
}

# serve the project itself, passed through to the webserver, with ssl
# exclude the compiled projects to prevent cross-domain issues
server {
        server_name viola.colinalworth.com;

        include letsencrypt.conf;

        ssl_certificate /etc/letsencrypt/live/viola.colinalworth.com/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/viola.colinalworth.com/privkey.pem;

        location / {
                proxy_pass http://localhost:8000/;
        }

        location /compiled/ {
                return 404;
        }
}

# prevent compiled projects from being on the same domain as the app itself
server {
        server_name static.viola.colinalworth.com;

        include letsencrypt.conf;

        ssl_certificate /etc/letsencrypt/live/static.viola.colinalworth.com/fullchain.pem;
        ssl_certificate_key /etc/letsencrypt/live/static.viola.colinalworth.com/privkey.pem;

        location /compiled/ {
                proxy_pass http://localhost:8000/compiled/;
        }
}
