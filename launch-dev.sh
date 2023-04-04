#!/bin/bash


kgx --tab  --command="bash -c 'cd rabbitmq; docker-compose up; exec bash'" &
kgx --tab  --command="bash -c 'cd api/application/db; docker-compose up; exec bash'" &
kgx --tab  --command="bash -c 'cd frontend;source ~/.nvm/nvm.sh;npm start; exec bash'" &