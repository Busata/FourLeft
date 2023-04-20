#!/bin/bash


gnome-terminal --tab --title="RabbitMQ" -- bash -c 'cd rabbitmq; docker-compose up; exec bash' &
gnome-terminal --tab --title="Local DB - Server" -- bash -c 'cd api/application/db; docker-compose up; exec bash' &
gnome-terminal --tab --title="Frontend NG" -- bash -c 'cd frontend;source ~/.nvm/nvm.sh;npm start; exec bash' &
gnome-terminal --tab --title="Local DB - Discord" -- bash -c 'cd discord-client/db; docker-compose up' &
