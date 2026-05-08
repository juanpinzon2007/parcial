#!/bin/sh
set -e

cat <<EOF >/etc/nginx/conf.d/default.conf
server {
  listen ${PORT:-80};
  server_name _;

  root /usr/share/nginx/html;
  index index.html;

  location / {
    try_files \$uri \$uri/ /index.html;
  }
}
EOF

cat <<EOF >/usr/share/nginx/html/runtime-config.js
window.__APP_CONFIG__ = {
  authApiUrl: '${AUTH_API_URL:-http://localhost:8081/api}',
  inventoryApiUrl: '${INVENTORY_API_URL:-http://localhost:8082/api}',
  salesApiUrl: '${SALES_API_URL:-http://localhost:8083/api}'
};
EOF

exec nginx -g 'daemon off;'
