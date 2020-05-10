#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import platform
import sys

from sshmanager.manager import Manager
from sshmanager.constants import PROGRAM

if platform.system() != 'Linux':
    print("currently only support running Linux!")
    sys.exit(-1)

parser = argparse.ArgumentParser(prog="python -m {}".format(PROGRAM),
                                 description='Auto invalid expired ssh authorized keys.')
parser.add_argument('-c', '--config', dest='config', metavar='config', default="/etc/ssh-manager/config.yaml",
                    type=str, help="config file for locating config of ssh public keys")
parser.add_argument('--verify', action="store_true", help="verify the config of ssh public keys")
args = parser.parse_args()

manager = Manager(args.config)

if args.verify:
    manager.verify_config()
else:
    manager.run()
