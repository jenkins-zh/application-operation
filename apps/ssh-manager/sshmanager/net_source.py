# -*- coding: utf-8 -*-
import base64
import os.path
import shutil
import tempfile

import requests

from sshmanager.constants import SSH_CONFIG_TMP_DIR
from sshmanager.logger import logger


class NetSource:

    def __init__(self, url, local_dir=None):
        self.session = requests.session()
        self.url = url
        if local_dir is None:
            self.local_dir = os.path.join(tempfile.gettempdir(), SSH_CONFIG_TMP_DIR)
        else:
            self.local_dir = local_dir
        logger.info("use dir {} for downloading ssh pub key configs".format(self.local_dir))

    def retrieve(self, url):
        response = self.session.get(url, timeout=30, verify=False)
        if response.ok:
            return base64.b64decode(response.json().get("content"))

    def get_targets(self):
        targets = []
        response = self.session.get(self.url, timeout=30, verify=False)
        if response.ok:
            for item in response.json():
                if item.get("type") == 'file':
                    targets.append((item.get("name"), item.get("type"), item.get("url")))
        logger.info("find targets {}".format(targets))
        return targets

    def retrieve_all(self):
        if os.path.exists(self.local_dir):
            shutil.rmtree(self.local_dir)
        os.mkdir(self.local_dir)
        targets = self.get_targets()
        for target in targets:
            with open(os.path.join(self.local_dir, target[0]), "wb+") as f:
                f.write(self.retrieve(target[2]))
            logger.info("retrieve {} successfully".format(target[2]))
