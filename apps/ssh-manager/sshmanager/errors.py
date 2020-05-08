# -*- coding: utf-8 -*-


class InvalidConfigException(Exception):
    """
    base class for invalid config exceptions.
    """
    def __init__(self, filename):
        self.filename = filename

    def __repr__(self):
        return 'invalid config file: %s' % self.filename

    def __str__(self):
        return 'invalid config file: %s' % self.filename
