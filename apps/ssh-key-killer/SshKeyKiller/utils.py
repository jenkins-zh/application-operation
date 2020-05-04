# -*- coding: utf-8 -*-
from datetime import datetime
import glob
import os.path
import stat

import yaml

from .errors import InvalidConfigException


def load_config(filename):
    """
    load config file or all the config files if filename represents a directory.
    """
    s = os.stat(filename).st_mode
    if stat.S_ISREG(s):
        return load_one_config(filename)
    elif stat.S_ISDIR(s):
        return load_all_config(filename)
    else:
        raise InvalidConfigException("invalid config file")


def load_one_config(config_file):
    """
    load and verify the config_file.
    """
    try:
        with open(config_file) as f:
            data = yaml.safe_load(f)
    except Exception:
        raise
    if not verify_config(data):
        raise InvalidConfigException("invalid config file")
    return data


def load_all_config(config_dir):
    """
    load all the config files in config_dir.
    """
    merged = {"hosts": []}
    files = glob.glob(os.path.join(config_dir, '**/*.y*ml'), recursive=True)
    for file in files:
        config = load_one_config(file).get("hosts")
        if config is not None:
            merged["hosts"].extend(config)
    return merged


def scan_authorized_keys():
    """
    return all the authorized_keys files by searching paths of `/home` and `/root`.
    """
    files = []
    authorized_keys_file = ".ssh/authorized_keys"
    files_tmp = [os.path.join("/home", user, authorized_keys_file) for user in os.listdir("/home")]
    files_tmp.append(os.path.join("/root", authorized_keys_file))
    for file in files_tmp:
        try:
            s = os.stat(file).st_mode
            if stat.S_ISREG(s):
                files.append(file)
        except OSError:
            pass
    return files


def verify_config(config):
    """
    verify the configuration.
    """
    hosts = config.get("hosts")
    if hosts is None:
        return False
    for host in hosts:
        if host.get("annotation") is None or host.get("pub_key") is None:
            return False
        try:
            datetime.strptime(str(host.get("expire_date")), "%Y%m%d")
        except ValueError:
            return False
    return True
