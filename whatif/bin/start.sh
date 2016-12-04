#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars what-if"

SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/whatif) java -jar target/whatif-0.7.0.jar
