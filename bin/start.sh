#!/bin/bash
function tabname {
  echo -n -e "\033]0;$1\007"
}
tabname "oscars"

orig_dir=`pwd`

top_dir="$(dirname "$0")/.."
cd "$top_dir"
top_dir=`pwd`

cd "$top_dir/core"

# set a trap on SIGINT to kill the first background task (the DS process) then exit
trap 'kill %1; kill %2 echo -e "\nExiting.."; exit' SIGINT
echo "Starting core.."
java -jar target/core-0.7.0.jar &


# keep polling core until curl exits OK, then it's safe to start the other processes

curl -k -s https://oscars:oscars-shared@localhost:8000/configs/ready > /dev/null
while [ $? -ne 0 ]; do
  sleep 1
  curl -k -s https://oscars:oscars-shared@localhost:8000/configs/ready > /dev/null
done


echo "Starting web UI.."
cd "$top_dir/webui"
SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui) java -jar target/webui-0.7.0.jar

kill %1; kill %2

cd ${orig_dir}

