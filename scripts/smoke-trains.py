#!/usr/bin/env python3
"""Native train checks in the prepared loopback development world.
Reserves x=6..20, y=7..11, z=6..22; modifies only test-tagged trains and that fixture.
"""
import json
import re
import runpy
import time
from pathlib import Path

root=Path(__file__).resolve().parents[1]
helpers=runpy.run_path(str(root/'scripts/smoke-server.py'))
helpers['wait_ready']()
# Keep one authenticated session while building the fixture; opening a connection per
# rail can exceed the legacy RCON listener's short-lived connection capacity.
import socket,atexit
connection=socket.create_connection(('127.0.0.1',int(helpers['properties'].get('rcon.port','25575'))),timeout=10)
atexit.register(connection.close)
helpers['send'](connection,1,3,helpers['password'])
if helpers['receive'](connection)[0]!=1:raise RuntimeError('Local RCON authentication failed')
sequence=1
def command(text):
    global sequence
    sequence+=1
    helpers['send'](connection,sequence,2,text)
    request,_,response=helpers['receive'](connection)
    if request!=sequence:raise RuntimeError('Unexpected RCON response')
    return response
selector='@e[type=create:train_carriage,tag=create_train_smoke,c=1]'

def state():
    response=command(f'entitydata {selector} {{}}')
    output={}
    for name in ['TrackX','TrackY','TrackZ','Forward','Progress','TrainSpeed','Cruise']:
        match=re.search(r'\b'+name+r':([-+\d.eE]+)',response)
        if not match:raise AssertionError(f'Missing {name} in train state: {response}')
        output[name]=float(match.group(1))
    return output

def check(condition,message):
    if not condition:raise AssertionError(message)
    print('PASS:',message,flush=True)

def wait_stopped():
    deadline=time.monotonic()+5
    while time.monotonic()<deadline:
        current=state()
        if abs(current['TrainSpeed'])<1e-6:return current
        time.sleep(.2)
    raise AssertionError('Train did not stop')

import argparse
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--verify-persistence',action='store_true')
args=parser.parse_args()
snapshot=root/'build/train-smoke-state.json'
if args.verify_persistence:
    expected=json.loads(snapshot.read_text());actual=state()
    for key in expected:check(abs(expected[key]-actual[key])<1e-6,f'{key} survived restart')
    raise SystemExit(0)

command('kill @e[type=create:train_carriage,tag=create_train_smoke]')
command('fill 6 8 6 20 11 22 minecraft:air')
command('fill 6 7 6 20 7 22 minecraft:stone')
for x in range(7,18):
    for z in [6,18]:command(f'setblock {x} 8 {z} create:track 1')
for z in range(7,18):
    for x in [6,18]:command(f'setblock {x} 8 {z} create:track 0')
for x,z,shape in [(6,6,6),(18,6,7),(18,18,8),(6,18,9)]:command(f'setblock {x} 8 {z} create:track {shape}')
for x,z,shape in [(6,6,6),(18,6,7),(18,18,8),(6,18,9)]:
    check('Successfully found' in command(f'testforblock {x} 8 {z} create:track {shape}'),'loop corner shape '+str((x,z)))
response=command('summon create:train_carriage 8.5 8.15 6.5 {Tags:["create_train_smoke"],TrackX:8,TrackY:8,TrackZ:6,Progress:0.5d,Forward:1,Cruise:0.6d}')
check('successfully summoned' in response.lower(),'native train entity registered and summoned')
visited=set();moving=False
for i in range(32):
    current=state();visited.add((int(current['TrackX']),int(current['TrackZ'])))
    moving|=current['TrainSpeed']>.1
    check(6<=current['TrackX']<=18 and 6<=current['TrackZ']<=18,'train remains on its route') if i==0 else None
    time.sleep(.5)
check(moving,'train accelerates under throttle')
check(any(x==18 and z>10 for x,z in visited),'train follows the first curve onto the east side')
check(any(z==18 and x<14 for x,z in visited),'train follows a second curve onto the south side')
check(any(x==6 for x,z in visited),'train continues around the loop')
command(f'entitydata {selector} {{Cruise:0.0d}}')
wait_stopped();check(True,'releasing throttle coasts to a stop')
command(f'entitydata {selector} {{Cruise:-0.6d}}');time.sleep(2)
check(state()['TrainSpeed']<-.1,'reverse throttle drives the train backwards')
command(f'entitydata {selector} {{Cruise:0.0d}}');wait_stopped()
command('kill @e[type=create:train_carriage,tag=create_train_smoke]')
for x in range(6,19):command(f'setblock {x} 8 22 create:track 1')
command('summon create:train_carriage 16.5 8.15 22.5 {Tags:["create_train_smoke"],TrackX:16,TrackY:8,TrackZ:22,Progress:0.5d,Forward:1,Cruise:1.0d}')
time.sleep(3)
current=state();check(current['TrackX']==18 and current['Progress']==1 and current['TrainSpeed']==0,'end of track stops the train without leaving the rail')
command('save-all flush');snapshot.parent.mkdir(exist_ok=True);snapshot.write_text(json.dumps(current,indent=2)+'\n')
print('Native train checks passed; stopped train saved for restart verification.',flush=True)
