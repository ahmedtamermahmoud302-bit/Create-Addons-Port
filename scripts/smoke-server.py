#!/usr/bin/env python3
"""Exercise an already-running local development server using its local RCON binding.
Reserves the fixture positions (-1..2, 8, 0) in the development world.
Reads its generated RCON password from ignored local config; never logs it.
"""
import socket
import struct
import time
from pathlib import Path

root = Path(__file__).resolve().parents[1]
properties = {}
for line in (root / 'run/server/server.properties').read_text().splitlines():
    if '=' in line and not line.startswith('#'):
        key, value = line.split('=', 1)
        properties[key.strip()] = value.strip()
if properties.get('server-ip') != '127.0.0.1' or properties.get('enable-rcon') != 'true':
    raise SystemExit('Smoke checks require the development server and RCON bound to loopback.')
password = properties.get('rcon.password')
if not password:
    raise SystemExit('A local RCON password must be configured securely before running this check.')

def receive(connection):
    def exact(length):
        output = bytearray()
        while len(output) < length:
            part = connection.recv(length-len(output))
            if not part:
                raise RuntimeError('RCON connection closed before a complete response')
            output.extend(part)
        return bytes(output)
    size = struct.unpack('<i', exact(4))[0]
    if not 10 <= size <= 1048576:
        raise RuntimeError('Invalid RCON response size')
    payload = exact(size)
    request_id, kind = struct.unpack('<ii', payload[:8])
    return request_id, kind, payload[8:-2].decode('utf-8', errors='replace')

def send(connection, request_id, kind, text):
    payload = struct.pack('<ii', request_id, kind)+text.encode()+b'\0\0'
    connection.sendall(struct.pack('<i', len(payload))+payload)

def command(text):
    with socket.create_connection(('127.0.0.1', int(properties.get('rcon.port', '25575'))), timeout=3) as connection:
        send(connection, 1, 3, password)
        request_id, _, _ = receive(connection)
        if request_id != 1:
            raise RuntimeError('Local RCON authentication failed')
        send(connection, 2, 2, text)
        request_id, _, result = receive(connection)
        if request_id != 2:
            raise RuntimeError('Unexpected RCON response')
        return result

def check(x, speed, status):
    text = f'testforblock {x} 8 0 create:shaft 0 {{RPM:{speed:.1f}f,Status:"{status}"}}'
    result = command(text)
    if 'Successfully found' not in result:
        raise AssertionError(f'{text}: {result}')
    print(f'PASS: shaft at x={x}: {speed} RPM, {status}')

def wait_ready():
    deadline=time.monotonic()+30
    while True:
        try:
            response=command('list')
            if 'players online' not in response:
                raise RuntimeError('Unexpected server response: '+response)
            return
        except (ConnectionError,TimeoutError,OSError):
            if time.monotonic() >= deadline: raise
            time.sleep(.5)

def smoke():
    for x in (-1, 1, 2):
        command(f'setblock {x} 8 0 create:shaft 0')
    command('setblock 0 8 0 create:creative_motor 5')
    command('blockdata 0 8 0 {GeneratedRPM:16}')
    # Wait for the network's ten-tick component update, not just an open port.
    time.sleep(1)
    check(1, 16, 'RUNNING')
    check(2, 16, 'RUNNING')
    check(-1, 0, 'UNPOWERED')
    command('blockdata 0 8 0 {GeneratedRPM:-32}')
    time.sleep(1)
    check(1, -32, 'RUNNING')
    check(2, -32, 'RUNNING')
    command('setblock 1 8 0 minecraft:air')
    time.sleep(1)
    check(2, 0, 'UNPOWERED')
    command('save-all flush')
    print('All six in-game kinetic assertions passed; test fixture saved.')

if __name__ == '__main__':
    import argparse
    parser=argparse.ArgumentParser(description=__doc__)
    group=parser.add_mutually_exclusive_group()
    group.add_argument('--verify-persistence',action='store_true')
    group.add_argument('--stop',action='store_true')
    options=parser.parse_args()
    wait_ready()
    if options.stop:
        print(command('stop'))
    elif options.verify_persistence:
        response=command('testforblock 0 8 0 create:creative_motor 5 {GeneratedRPM:-32}')
        if 'Successfully found' not in response:raise AssertionError(response)
        check(2,0,'UNPOWERED')
        print('PASS: motor setting and disconnected shaft state survived restart')
    else:
        smoke()
