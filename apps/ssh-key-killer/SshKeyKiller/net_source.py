# -*- coding: utf-8 -*-
import base64
import glob
import shutil
import tempfile
import os.path

import requests
import yaml


class NetSource:

    def __init__(self, filename, local_dir=None):
        self.filename = filename
        if local_dir is None:
            self.local_dir = os.path.join(tempfile.gettempdir(), 'ssh@tmp')
        else:
            self.local_dir = local_dir

    @staticmethod
    def retrieve(filename):
        with requests.get(filename, timeout=30) as response:
            if response.ok:
                data = base64.b64decode(response.json().get("content"))
        return data

    def get_targets(self):
        targets = []
        with requests.get(self.filename, timeout=30) as response:
            if response.ok:
                for item in response.json():
                    if item.get("type") == 'file':
                        targets.append((item.get("name"), item.get("type"), item.get("url")))
        return targets

    def retrieve_all(self):
        if os.path.exists(self.local_dir):
            shutil.rmtree(self.local_dir)
        os.mkdir(self.local_dir)
        targets = self.get_targets()
        for target in targets:
            with open(os.path.join(self.local_dir, target[0]), "wb+") as f:
                f.write(self.retrieve(target[2]))

    def merged(self):
        merged = {}
        files = glob.glob(os.path.join(self.local_dir, '**/*.y*ml'), recursive=True)
        for file in files:
            with open(file) as f:
                data = yaml.safe_load(f)
            config = load_one_config(file).get("hosts")
            if config is not None:
                merged["hosts"].extend(config)


if __name__ == '__main__':
    repo = "anxk/application-operation"
    branch = "anxk0411"
    uri = "https://api.github.com/repos/{}/contents/config/ssh?ref={}".format(repo, branch)
    print(uri)
    n = NetSource(uri)
    print(tempfile.gettempdir())
    n.retrieve_all()
