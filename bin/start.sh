#!/bin/bash

orig_dir=`pwd`

top_dir="$(dirname "$0")/.."
cd "$top_dir"
top_dir=`pwd`

cd "$top_dir/ds"

# set a trap on SIGINT to kill the first background task (the DS process) then exit
trap 'kill %1; echo -e "\nExiting.."; exit' SIGINT
echo "Starting data store.."
java -jar target/ds-0.7.0.jar &

# keep polling the DS until curl exits OK, then it's safe to start the other processes
curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui > /dev/null
while [ $? -ne 0 ]; do
  sleep 1
  curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui > /dev/null
done

# overwrite the trap to make it kill the first two bg tasks (the DS and webui processes) then exit
trap 'kill %1; kill %2; echo -e "\nExiting..\n\n\n"; exit' SIGINT

echo "Starting web UI.."
cd "$top_dir/webui"
SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/webui) java -jar target/webui-0.7.0.jar &

echo "Starting core.."

cd "$top_dir/core"
SPRING_APPLICATION_JSON=$(curl -k -s https://oscars:oscars-shared@localhost:8000/configs/get/core) java -jar target/core-0.7.0.jar

kill %1; kill %2

cd ${orig_dir}

