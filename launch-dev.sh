#!/bin/bash


gnome-terminal --tab --title="RabbitMQ" -- bash -c 'cd devops/rabbitmq; docker-compose up; exec bash' &
gnome-terminal --tab --title="Local DB - Server" -- bash -c 'cd backend/db; docker-compose up; exec bash' &
gnome-terminal --tab --title="Frontend NG" -- bash -c 'cd frontend;source ~/.nvm/nvm.sh;npm start; exec bash' &
gnome-terminal --tab --title="Local DB - Discord" -- bash -c 'cd discord/db; docker-compose up' &
