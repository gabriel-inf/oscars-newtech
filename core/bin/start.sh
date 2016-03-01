#!/bin/bash
SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/core) java -jar target/core-0.7.0.jar
