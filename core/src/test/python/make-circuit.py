#! /usr/bin/env python

import argparse
import fileinput
import requests
from requests.auth import HTTPBasicAuth
import json
import time

connectionJsonDefault = '{"id":null,"connectionId":"dMZvyve","states":{"resv":"SUBMITTED","prov":"INITIAL","oper":"ADMIN_DOWN_OPER_DOWN"},"schedule":{"submitted":1489091133987,"setup":1489091100000,"teardown":1489177500000},"reservedSchedule":[],"specification":{"id":null,"version":0,"username":"some user","description":"fdsafasd","containerConnectionId":"dMZvyve","scheduleSpec":{"startDates":[1489091100000],"endDates":[1489177500000],"minimumDuration":0},"requested":{"id":null,"vlanFlow":{"id":null,"junctions":[],"pipes":[{"id":null,"aJunction":{"id":null,"deviceUrn":"snll-rt2","junctionType":"REQUESTED","fixtures":[{"id":null,"portUrn":"snll-rt2:t3-0/0/0","vlanId":null,"vlanExpression":"2-4094","fixtureType":"REQUESTED","inMbps":1,"egMbps":1}]},"zJunction":{"id":null,"deviceUrn":"eqx-sj-rt1","junctionType":"REQUESTED","fixtures":[{"id":null,"portUrn":"eqx-sj-rt1:ge-0/0/0","vlanId":null,"vlanExpression":"2-4094","fixtureType":"REQUESTED","inMbps":1,"egMbps":1}]},"azMbps":0,"zaMbps":0,"azERO":[],"zaERO":[],"urnBlacklist":[],"pipeType":"REQUESTED","eroPalindromic":"PALINDROME","eroSurvivability":"SURVIVABILITY_NONE","numDisjoint":1}],"minPipes":1,"maxPipes":1,"containerConnectionId":"dMZvyve"},"layer3Flow":{"id":null,"junctions":[],"pipes":[]},"containerConnectionId":"dMZvyve"}},"reserved":{"id":null,"vlanFlow":{"id":null,"junctions":[],"ethPipes":[],"mplsPipes":[],"allPaths":null,"containerConnectionId":"dMZvyve"},"containerConnectionId":"dMZvyve"}}'

parser = argparse.ArgumentParser(description='OSCARS circuit creator')
parser.add_argument('-p', '--password', help='password for authorization', default='oscars-shared')
parser.add_argument('-u', '--user', help='username for authorization', default='oscars')
parser.add_argument('-U', '--url', help='base URL for reservation', default='https://localhost:8000/')
parser.add_argument('-v', '--verbose', help='verbose', action='store_true')

parser.add_argument('-f', '--file', help='file containing JSON specification for new reservation (default: use internal template)')

parser.add_argument('--id', help='set connection ID for new request')
parser.add_argument('--submitTime', help='set submission time (default: now)')
parser.add_argument('--setupTime', help='set setup time (default: now)')
parser.add_argument('--teardownTime', help='set teardown time (default: now + 1 hour)')
parser.add_argument('-b', '--bitrate', help='set circuit bitrate (default:  1 Mbps)', default='1')
parser.add_argument('--aport', help='set A port', default='lbl-mr2:xe-0/1/0')
parser.add_argument('--zport', help='set Z port', default='bnl-mr2:xe-1/0/0')

args = parser.parse_args()
# print args
if args.verbose:
    print "Arguments: " + str(args)


# Don't spam stderr with warnings about unverifiable certificates.  This is also the
# reason for any verify=False parameters in calls to the requests module in
# code below.  This is undocumented but mentioned in:
# https://github.com/kennethreitz/requests/issues/2214
requests.packages.urllib3.disable_warnings()

# Slurp in input file if given
if (args.file):
    indatalines = fileinput.input(args.file)
    indata = ''.join(indatalines)
else:
    indata = connectionJsonDefault
    
if args.verbose:
    print "Input file:"
    print indata

connection = json.loads(indata)
if args.verbose:
    print "Object (as read): " + str(connection)
    
# Set submitted time
if args.submitTime:
    submitTime = int(args.submitTime) * 1000
else:
    submitTime = int(time.time()) * 1000
connection['schedule']['submitted'] = submitTime

# Set setup time
if args.setupTime:
    setupTime = int(args.setupTime) * 1000
else:
    setupTime = int(time.time()) * 1000
connection['schedule']['setup'] = setupTime
connection['specification']['scheduleSpec']['startDates'][0] = setupTime

# Set teardown time
if args.teardownTime:
    teardownTime = int(args.teardownTime) * 1000
else:
    teardownTime = int(time.time() + 3600) * 1000
connection['schedule']['teardown'] = teardownTime
connection['specification']['scheduleSpec']['endDates'][0] = teardownTime

# Set connectionId if present
if args.id:
    connection['connectionId'] = args.id
    connection['specification']['containerConnectionId'] = args.id
    connection['specification']['requested']['vlanFlow']['containerConnectionId'] = args.id
    connection['specification']['requested']['containerConnectionId'] = args.id
    connection['reserved']['vlanFlow']['containerConnectionId'] = args.id
    connection['reserved']['containerConnectionId'] = args.id
    
# Set bitrate
connection['specification']['requested']['vlanFlow']['pipes'][0]['aJunction']['fixtures'][0]['inMbps'] = args.bitrate
connection['specification']['requested']['vlanFlow']['pipes'][0]['aJunction']['fixtures'][0]['egMbps'] = args.bitrate
connection['specification']['requested']['vlanFlow']['pipes'][0]['zJunction']['fixtures'][0]['inMbps'] = args.bitrate
connection['specification']['requested']['vlanFlow']['pipes'][0]['zJunction']['fixtures'][0]['egMbps'] = args.bitrate

# Set A and Z junction fixtures
adev = args.aport.split(':')[0]
connection['specification']['requested']['vlanFlow']['pipes'][0]['aJunction']['deviceUrn'] = adev
connection['specification']['requested']['vlanFlow']['pipes'][0]['aJunction']['fixtures'][0]['portUrn'] = args.aport

zdev = args.zport.split(':')[0]
connection['specification']['requested']['vlanFlow']['pipes'][0]['zJunction']['deviceUrn'] = zdev
connection['specification']['requested']['vlanFlow']['pipes'][0]['zJunction']['fixtures'][0]['portUrn'] = args.zport


if args.verbose:
    print "Object (ready to send): " + str(connection)

outdata = json.dumps(connection)

r = requests.post(args.url + '/resv/connection/add', auth=HTTPBasicAuth(args.user, args.password), data=outdata, verify=False, headers={'Content-Type' : 'application/json'})
if args.verbose:
    print "status " + str(r.status_code) + ": " + r.text
if r.status_code != requests.codes.ok:
    print "error:  " + r.text
    exit(1)

r = requests.get(args.url + 'resv/commit/' + connection['connectionId'], auth=HTTPBasicAuth(args.user, args.password), verify=False)
if args.verbose:
    print "status " + str(r.status_code) + ": " + r.text
if r.status_code != requests.codes.ok:
    print "error:  " + r.text
    exit(1)

r = requests.get(args.url + '/resv/all', auth=HTTPBasicAuth(args.user, args.password), verify=False)
if args.verbose:
    print str(r.text)
obj = r.json()
for o in obj:
    print str(o['id']) + " : " + o['connectionId']



