#!/usr/bin/env python
import argparse
import platform
import sys


if platform.system() != 'Linux':
    sys.exit(-1)


from .killer import Killer

parser = argparse.ArgumentParser(description='ssh key killer')
parser.add_argument('--config', dest='config', metavar='configfile', default="/etc/ssh-key-killer/config.yaml", type=str)
args = parser.parse_args()

Killer(configfile=args.config).invalid()
