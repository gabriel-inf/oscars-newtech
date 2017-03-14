import os
import subprocess
import time
import sys
import logging
import traceback
import requests

processes = []
def execute_cmd(cmd):
    proc = subprocess.Popen(cmd)
    return proc


def handle_signal(signum, frame):
    murder()

def murder():
    for proc in processes:
        proc.kill()
    sys.exit()

def poll_for_module(url):
    wait = True
    while wait:
        time.sleep(1)
        try:
            cfg = requests.get(url, auth=("oscars", "oscars-shared"), verify=False, timeout=1).json()
            wait = False
        except Exception as e:
            logging.info("Configuration params not accessible yet, waiting...")
            #logging.error(traceback.format_exc())

try:
    orig_dir = os.getcwd()

    top_dir = os.path.join(os.path.dirname(__file__), "..")

    os.chdir(top_dir)
    top_dir = os.getcwd()

    # Trap SIGINT
    # signal.signal(signal.SIGINT, handle_signal)

    # Launch core
    core_dir = os.path.join(top_dir, "core")
    os.chdir(core_dir)
    core_target = os.path.join(core_dir, "target", "core-0.7.0.jar")
    core_cmd = ['java', "-jar", core_target]
    core_proc = execute_cmd(core_cmd)
    processes.append(core_proc)

    # Keep polling core until curl exist OK, then it's safe to start the other processes
    poll_for_module("https://oscars:oscars-shared@localhost:8000/configs/get/whatif")
    print("\nWhat-if config polled")
    poll_for_module("https://oscars:oscars-shared@localhost:8000/configs/get/webui")
    print("\nWebUI config polled")

    # Launch whatif
    whatif_dir = os.path.join(top_dir, "whatif")
    os.chdir(whatif_dir)
    whatif_target = os.path.join(whatif_dir, "target", "whatif-0.7.0.jar")
    whatif_cfg = requests.get("https://localhost:8000/configs/get/whatif", auth=("oscars", "oscars-shared"), verify=False).text
    whatif_cfg = whatif_cfg.replace('"', '\"')
    whatif_cmd = ['java', "-Dspring.application.json=" + whatif_cfg,  "-jar", whatif_target]
    whatif_proc = execute_cmd(whatif_cmd)
    processes.append(whatif_proc)

    # Launch webui
    webui_dir = os.path.join(top_dir, "webui")
    os.chdir(webui_dir)
    webui_target = os.path.join(webui_dir, "target", "webui-0.7.0.jar")
    web_cfg = requests.get("https://localhost:8000/configs/get/webui", auth=("oscars", "oscars-shared"), verify=False).text
    web_cfg = web_cfg.replace('"', '\"')
    web_cmd = ['java', "-Dspring.application.json=" + web_cfg,  "-jar", webui_target]
    web_proc = execute_cmd(web_cmd)
    processes.append(web_proc)

    os.chdir(orig_dir)
    while 1:
        time.sleep(1)
except Exception as e:
    logging.error(traceback.format_exc())
    murder()

