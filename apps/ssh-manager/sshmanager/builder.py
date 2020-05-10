# -*- coding: utf-8 -*-
import os.path
import shutil
import tempfile
from datetime import datetime

from sshmanager.constants import *
from sshmanager.logger import logger
from sshmanager.utils import load_config


class Builder:

    def __init__(self, filename=None, local_dir=None):
        if filename is None:
            self.filename = os.path.join(tempfile.gettempdir(), SSH_CONFIG_TMP_DIR)
        else:
            self.filename = filename

        if local_dir is None:
            self.local_dir = os.path.join(tempfile.gettempdir(), SSH_AUTH_BUILD_DIR)
        else:
            self.local_dir = local_dir
        logger.info("use dir {} for building authorized_keys file".format(self.local_dir))
        self.data = {}
        self.config = load_config(self.filename)

    @staticmethod
    def is_expired(host):
        expire_date = host.get("expire_date")
        if datetime.strptime(str(expire_date), "%Y%m%d") < datetime.now():
            logger.info("{} was expired, expire date: {}".format(host, expire_date))
            return True
        return False

    def build(self):
        hosts = self.config.get("hosts")
        for host in hosts:
            if self.is_expired(host):
                host["pub_key"] = ""
            user = host.get("user")
            if self.data.get(user) is None:
                self.data[user] = ""
            self.data[user] += host.get("pub_key") + '\n'
        self.dump()

    def dump(self):
        if os.path.exists(self.local_dir):
            shutil.rmtree(self.local_dir)
        os.mkdir(self.local_dir)
        for k, v in self.data.items():
            with open(os.path.join(self.local_dir, k), "w+") as f:
                f.writelines(v)
            logger.info("build and dump authorized_keys for user {} successfully".format(k))
