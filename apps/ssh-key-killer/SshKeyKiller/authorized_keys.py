# -*- coding: utf-8 -*-
import shutil
import tempfile


class AuthorizedKeys:
    """
    AuthorizedKeys represents the authorized_keys file used by sshd to
    authorize user's activities.
    """
    def __init__(self, filename):
        self.filename = filename
        self.data = []
        self._parse()

    def _parse(self):
        """
        read authorized_keys file.
        """
        with open(self.filename, "r") as f:
            self.data = [line.strip(" \n") for line in f.readlines()]

    def invalid(self, pub_key):
        """
        invalid the pub_key by removing it.
        """
        self.data.remove(pub_key)

    def save(self):
        """
        save the modifications of authorized_keys in memory to disk.
        """
        tmp = tempfile.mkstemp()[1]
        with open(tmp, "w") as f:
            f.writelines(line for line in self.data)
        shutil.move(tmp, self.filename)
