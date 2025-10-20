#!/bin/bash

# Salir si ocurre algún error
set -e

echo "Construyendo la imagen maven-base..."
docker compose build maven-base

echo "Construyendo todas las demás imágenes..."
docker compose build

echo "Iniciando los contenedores..."
docker compose up
