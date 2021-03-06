#! /usr/bin/env python

import argparse
import fileinput
import requests
from requests.auth import HTTPBasicAuth

parser = argparse.ArgumentParser(description='OSCARS circuit creator')
parser.add_argument('-p', '--password', help='password for authorization', default='oscars-shared')
parser.add_argument('-u', '--user', help='username for authorization', default='oscars')
parser.add_argument('-U', '--url', help='base URL for reservation', default='https://localhost:8000/')
parser.add_argument('-v', '--verbose', help='verbose', action='store_true')

parser.add_argument('--id', help='set connection ID for new request')

args = parser.parse_args()

# Don't spam stderr with warnings about unverifiable certificates.  This is also the
# reason for any verify=False parameters in calls to the requests module in
# code below.  This is undocumented but mentioned in:
# https://github.com/kennethreitz/requests/issues/2214
requests.packages.urllib3.disable_warnings()

if args.id:
    r = requests.get(args.url + '/resv/get/' + args.id, auth=HTTPBasicAuth(args.user, args.password), verify=False)
    print str(r.text)
else:
    r = requests.get(args.url + '/resv/all', auth=HTTPBasicAuth(args.user, args.password), verify=False)
    if args.verbose:
        print str(r.text)
    obj = r.json()
    for o in obj:
        print str(o['id']) + " : " + o['connectionId']



