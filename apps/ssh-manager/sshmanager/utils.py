# -*- coding: utf-8 -*-
from datetime import datetime
import glob
import os.path
import stat

import yaml

from sshmanager.errors import InvalidConfigException


def load_yaml(filename):
    with open(filename) as f:
        return yaml.safe_load(f)


def load_config(filename):
    """
    loads a config file or all the config files if filename represents a directory.
    """
    s = os.stat(filename).st_mode
    if stat.S_ISREG(s):
        return load_one_config(filename)
    elif stat.S_ISDIR(s):
        return load_all_config(filename)
    else:
        raise InvalidConfigException


def load_one_config(config_file):
    """
    loads and verifies the config file.
    """
    try:
        with open(config_file) as f:
            data = yaml.safe_load(f)
    except Exception:
        raise InvalidConfigException(config_file)
    if not verify_config(data):
        raise InvalidConfigException(config_file)
    return data


def load_all_config(config_dir):
    """
    loads all the config files in config dir.
    """
    merged = {"hosts": []}
    files = glob.glob(os.path.join(config_dir, '**/*.y*ml'), recursive=True)
    for file in files:
        config = load_one_config(file).get("hosts")
        if config is not None:
            merged["hosts"].extend(config)
    return merged


def verify_config(config):
    """
    verifies the config.
    """
    hosts = config.get("hosts")
    if hosts is None:
        return False
    for host in hosts:
        if host.get("annotation") is None or \
                host.get("pub_key") is None or \
                host.get("user") is None or \
                host.get("expire_date") is None:
            return False
        try:
            datetime.strptime(str(host.get("expire_date")), "%Y%m%d")
        except ValueError:
            return False
    return True
