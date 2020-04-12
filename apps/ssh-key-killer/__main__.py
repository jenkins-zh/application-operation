#!/usr/bin/env python
import argparse
import platform
import sys


if platform.system() != 'Linux':
    sys.exit(-1)


from .killer import Killer

parser = argparse.ArgumentParser(description='ssh key killer')
parser.add_argument('--config-dir', dest='config_dir', metavar='config_dir', default="/etc/ssh-key-killer/", type=str)
args = parser.parse_args()

Killer(configfile=args.config_dir).invalid()
