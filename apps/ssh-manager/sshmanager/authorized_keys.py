# -*- coding: utf-8 -*-
import shutil
import os
import os.path

from sshmanager.constants import SSH_AUTH_FILE_NAME
from sshmanager.logger import logger


class AuthorizedKeys:
    """
    AuthorizedKeys represents the authorized_keys file used by sshd.
    """
    def __init__(self, new_file, user):
        self.new_file = new_file
        self.user = user

    def home_dir(self):
        home_str = '~{}'.format(self.user)
        home = os.path.expanduser(home_str)
        return None if home == home_str else home

    def update(self):
        home = self.home_dir()
        if home is None:
            logger.warn("{} is not a valid user".format(self.user))
            return
        old_file = os.path.join(home, SSH_AUTH_FILE_NAME)
        if not os.path.exists(old_file):
            os.makedirs(os.path.dirname(old_file), exist_ok=True)
            with open(old_file, "wb+") as _:
                pass
            logger.info("create {} successfully".format(old_file))
        shutil.copyfile(self.new_file, old_file)
        logger.info("update {}".format(old_file))
