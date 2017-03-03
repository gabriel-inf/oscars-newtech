#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars PSS"

java -Xmx512m -jar target/pss-0.7.0.jar
