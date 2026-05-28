#!/usr/bin/env sh
set -eu

DOMAIN="${1:-}"
if [ -z "$DOMAIN" ]; then
  echo "Usage: scripts/deploy-https.sh your-domain.com"
  exit 1
fi

if [ ! -f deploy/certs/fullchain.pem ] || [ ! -f deploy/certs/privkey.pem ]; then
  echo "Missing deploy/certs/fullchain.pem or deploy/certs/privkey.pem"
  echo "Use certbot/acme.sh to issue certificates first."
  exit 1
fi

sed "s/example.com/$DOMAIN/g; s/www.example.com/www.$DOMAIN/g" deploy/nginx/blog.https.example.conf > deploy/nginx/blog.conf
docker compose -p enterprise-blog up -d --build
docker compose -p enterprise-blog exec nginx nginx -s reload
