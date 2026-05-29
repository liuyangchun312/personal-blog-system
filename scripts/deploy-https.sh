#!/usr/bin/env sh
set -eu

DOMAIN="${1:-}"
PROJECT="${COMPOSE_PROJECT:-enterprise-blog}"

if [ -z "$DOMAIN" ]; then
  echo "Usage: scripts/deploy-https.sh your-domain.com"
  exit 1
fi

if [ ! -f deploy/certs/fullchain.pem ] || [ ! -f deploy/certs/privkey.pem ]; then
  echo "Missing deploy/certs/fullchain.pem or deploy/certs/privkey.pem"
  echo "Use certbot/acme.sh to issue certificates first."
  exit 1
fi

export PUBLIC_BASE_URL="${PUBLIC_BASE_URL:-https://$DOMAIN}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-prod}"

sed "s/example.com/$DOMAIN/g; s/www.example.com/www.$DOMAIN/g" deploy/nginx/blog.https.example.conf > deploy/nginx/blog.conf
docker compose -p "$PROJECT" config --quiet
docker compose -p "$PROJECT" up -d --build
docker compose -p "$PROJECT" exec nginx nginx -s reload
echo "HTTPS deployment finished for https://$DOMAIN"
