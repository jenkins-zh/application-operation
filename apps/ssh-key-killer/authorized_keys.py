#!/usr/bin/env python


class AuthorizedKeys:
    def __init__(self, filename):
        self.filename = filename
        self.data = {}
        self._parse()

    def _parse(self):
        with open(self.filename) as f:
            temp = f.readlines()
        for line in temp:
            self.data[line] = None

    def invalid(self, key):
        self.data.pop(key)

    def save(self):
        with open(self.filename, "w+") as f:
            f.writelines(line for line in self.data.keys())
