#!/usr/bin/env python
import os.path
import stat

import yaml


def load_config(config):
    try:
        with open(config) as f:
            return yaml.safe_load(f)
    except Exception as e:
        raise e


def scan_authorized_keys():
    files = []
    home = "/home"
    authorized_keys_file = ".ssh/authorized_keys"
    files_tmp = [os.path.join(home, user, authorized_keys_file) for user in os.listdir(home)]
    files_tmp.append(os.path.join("/root", authorized_keys_file))
    for file in files_tmp:
        try:
            s = os.stat(file).st_mode
            if stat.S_ISREG(s):
                files.append(file)
        except OSError:
            pass
    return files
