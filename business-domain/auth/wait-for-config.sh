#!/bin/sh
# wait-for-config.sh

CONFIG_URL=http://config-server:8888/actuator/health
TIMEOUT=60       # segundos totales de espera
INTERVAL=2       # segundos entre intentos

echo "Esperando a que config-server esté listo en $CONFIG_URL..."

elapsed=0
while [ $elapsed -lt $TIMEOUT ]; do
  if curl -fs $CONFIG_URL > /dev/null 2>&1; then
    echo "Config-server listo!"
    exec "$@"
  fi
  echo "Config-server no responde todavía. Esperando $INTERVAL segundos..."
  sleep $INTERVAL
  elapsed=$((elapsed + INTERVAL))
done

echo "Tiempo de espera agotado: $CONFIG_URL no respondió"
exit 1
