#!/usr/bin/env python
from datetime import datetime

from .authorized_keys import AuthorizedKeys
from .logger import logger
from .utils import load_config, scan_authorized_keys


class Killer:
    def __init__(self, configfile):
        self.config = load_config(configfile)

    def get_invalid_hosts(self):
        invalid_hosts = []
        for host in self.config.get("hosts"):
            if datetime.strptime(str(host.get("expire_time")), "%Y%m%d") < datetime.now():
                invalid_hosts.append(host.get("host"))
        return invalid_hosts

    def invalid(self):
        authorized_keys_files = scan_authorized_keys()
        invalid_hosts = self.get_invalid_hosts()
        for file in authorized_keys_files:
            ak = AuthorizedKeys(file)
            for host in ak.data.keys():
                if host in invalid_hosts:
                    ak.invalid(host)
                    logger.info("invalid host {}".format(host))
            ak.save()
