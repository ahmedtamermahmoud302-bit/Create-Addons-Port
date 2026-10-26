#!/usr/bin/env python3
"""Live transmission regression. Reserves x=110..150,y=7..11,z=-2..12 in the ignored test world. Those chunks must be loaded."""
import runpy,socket,atexit,time,re
from pathlib import Path
base=runpy.run_path(str(Path(__file__).with_name('smoke-server.py')));base['wait_ready']()
sock=socket.create_connection(('127.0.0.1',int(base['properties'].get('rcon.port','25575'))),timeout=10);atexit.register(sock.close);base['send'](sock,1,3,base['password']);assert base['receive'](sock)[0]==1
sequence=1
def command(value):
 global sequence
 sequence+=1;base['send'](sock,sequence,2,value);request,_,text=base['receive'](sock);assert request==sequence;return text
def block(x,y,z,name,meta=0):
 response=command(f'setblock {x} {y} {z} {name} {meta}');assert 'outside' not in response.lower(), 'Load the transmission fixture chunks first: '+response
def motor(x,y,z,face=5,speed=32):block(x,y,z,'create:creative_motor',face);command(f'blockdata {x} {y} {z} {{GeneratedRPM:{speed}}}')
def speed(x,y,z,value):
 time.sleep(.7);response=command(f'blockdata {x} {y} {z} {{}}');m=re.search(r'\bRPM:([-+\d.eE]+)f',response);assert m and abs(float(m[1])-value)<.001,(value,response);print(f'PASS: {(x,y,z)} = {value} RPM',flush=True)
command('fill 110 8 -2 150 11 12 minecraft:air');command('fill 110 7 -2 150 7 12 minecraft:stone')
block(112,8,0,'create:cogwheel',1);block(113,8,0,'create:cogwheel',1);block(113,9,0,'create:shaft',1);motor(112,7,0,1);speed(113,9,0,-32)
block(117,8,0,'create:large_cogwheel',1);block(118,8,1,'create:cogwheel',1);block(118,9,1,'create:shaft',1);motor(117,7,0,1);speed(118,9,1,-64)
block(122,8,0,'create:large_cogwheel',0);block(123,9,0,'create:large_cogwheel',1);block(123,10,0,'create:shaft',1);motor(121,8,0);speed(123,10,0,32)
block(128,8,0,'create:gearbox',1);motor(127,8,0)
for x,z,axis in [(129,0,0),(128,-1,2),(128,1,2)]:block(x,8,z,'create:shaft',axis)
speed(129,8,0,-32);speed(128,8,-1,-32);speed(128,8,1,32)
block(134,8,0,'create:gearbox',0);motor(134,8,-1,3);block(134,9,0,'create:shaft',1);block(134,7,0,'create:shaft',1);speed(134,9,0,32);speed(134,7,0,-32)
block(140,8,0,'create:clutch',0);block(141,8,0,'create:shaft',0);motor(139,8,0);speed(141,8,0,32);block(140,8,1,'minecraft:redstone_block');speed(141,8,0,0)
response=command('blockdata 140 8 0 {}');ports=re.search(r'PortRPM:\[([^]]+)\]',response);assert ports and abs(float(ports[1].split(',')[4].rstrip('f'))-32)<.001;print('PASS: powered clutch retains rotating input shaft',flush=True)
block(140,8,1,'minecraft:air');speed(141,8,0,32)
block(145,8,0,'create:gearshift',0);block(146,8,0,'create:shaft',0);motor(144,8,0);speed(146,8,0,32);block(145,8,1,'minecraft:redstone_block');speed(146,8,0,-32)
block(112,8,10,'create:adjustable_chain_gearshift',1);block(113,8,10,'create:encased_chain_drive',1);block(113,9,10,'create:shaft',1);motor(112,7,10,1);speed(113,9,10,32);block(112,8,11,'minecraft:redstone_block');speed(113,9,10,64)
block(112,7,10,'minecraft:air');motor(113,7,10,1);block(112,9,10,'create:shaft',1);speed(112,9,10,16)
command('save-all flush');print('All live transmission checks passed.',flush=True)
