#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import platform
import sys


if platform.system() != 'Linux':
    sys.exit(-1)


from .killer import Killer
from .utils import load_config

parser = argparse.ArgumentParser(description='ssh key killer')
parser.add_argument('-c', '--config', dest='config', metavar='config', default="/etc/ssh-key-killer/",
                    type=str, help="a config file or a directory contains config files")
parser.add_argument('--verify', action="store_true", help="verify the config file")
args = parser.parse_args()

if args.verify:
    load_config(args.config)
else:
    Killer(configfile=args.config).invalid()
