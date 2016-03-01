#!/bin/bash

orig_dir=`pwd`

top_dir="$(dirname "$0")/.."
cd "$top_dir"
top_dir=`pwd`

cd "$top_dir/ds"

trap 'kill %1; echo -e "\nExiting.."; exit' SIGINT
echo "Starting data store.."
java -jar target/ds-0.7.0.jar &

curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui
while [ $? -ne 0 ]; do
  sleep 1
  curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui
done

trap 'kill %1; kill %2; echo -e "\nExiting..\n\n\n"; exit' SIGINT

echo "Starting web UI.."
cd "$top_dir/webui"
SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui) java -jar target/webui-0.7.0.jar &

echo "Starting core.."

cd "$top_dir/core"
SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/core) java -jar target/core-0.7.0.jar

cd $orig_dir

