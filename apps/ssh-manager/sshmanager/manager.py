# -*- coding: utf-8 -*-
import os
import tempfile

from sshmanager.net_source import NetSource
from sshmanager.builder import Builder
from sshmanager.authorized_keys import AuthorizedKeys
from sshmanager.constants import *
from sshmanager.utils import load_yaml, load_config
from sshmanager.logger import logger


class Manager:

    def __init__(self, config_file):
        self.config = load_yaml(config_file)
        self.url = "https://api.github.com/repos/{repo}/contents/{folder}?ref={branch}".format(**self.config)

    def run(self):
        NetSource(self.url).retrieve_all()
        Builder().build()
        for file in os.listdir(os.path.join(tempfile.gettempdir(), SSH_AUTH_BUILD_DIR)):
            auth = AuthorizedKeys(os.path.join(tempfile.gettempdir(), SSH_AUTH_BUILD_DIR, file), file)
            auth.update()
            logger.info("update authorized_keys file for user {} successfully".format(file))

    def verify_config(self):
        NetSource(self.url).retrieve_all()
        load_config(os.path.join(tempfile.gettempdir(), SSH_CONFIG_TMP_DIR))
        logger.info("verification pass!")
