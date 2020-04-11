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
            if line.strip(" \n") != "":
                self.data[line.strip(" \n").split(" ")[-1]] = line

    def invalid(self, host):
        self.data[host] = "x" + self.data.get(host)

    def save(self):
        with open(self.filename, "w+") as f:
            f.writelines(line for line in self.data.values())
