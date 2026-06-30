#!/bin/bash

echo "Stopping"
docker compose down
sleep 5

echo "Starting authenticator"
docker compose up spring.racenet-authenticator -d
echo "Waiting..."
sleep 120

echo "Starting backend"
docker compose up spring.fourleft.backend-ea-sports-wrc -d
echo "Waiting..."
sleep 60

echo "Starting other services"
docker compose up -d
