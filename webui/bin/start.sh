#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars web ui"

SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui) java -jar target/webui-0.7.0.jar
