# -*- coding: utf-8 -*-
import logging


logger = logging.getLogger("ssh-key-killer")
logger.setLevel(logging.INFO)

ch = logging.FileHandler("/var/log/ssh-key-killer.log")

formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
ch.setFormatter(formatter)
logger.addHandler(ch)
