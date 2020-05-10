# -*- coding: utf-8 -*-

import logging
import os.path
import tempfile

from sshmanager.constants import *

LOG_FILE = os.path.join(tempfile.gettempdir(), PROGRAM + ".log")

if not os.path.exists(LOG_FILE):
    os.makedirs(os.path.dirname(LOG_FILE), exist_ok=True)
    with open(LOG_FILE, "wb+") as _:
        pass

logger = logging.getLogger(PROGRAM)
logger.setLevel(logging.INFO)

ch_file = logging.FileHandler(LOG_FILE)
ch_stdout = logging.StreamHandler()
formatter = logging.Formatter(LOG_FORMAT)

ch_file.setFormatter(formatter)
ch_stdout.setFormatter(formatter)
logger.addHandler(ch_file)
logger.addHandler(ch_stdout)
