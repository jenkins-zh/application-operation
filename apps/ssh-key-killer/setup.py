from os import path

from setuptools import setup, find_packages

here = path.abspath(path.dirname(__file__))

with open(path.join(here, 'requirements.txt')) as f:
    requirements = f.readlines()

requirements = [requirement.replace('\n', '') for requirement in requirements]

setup(
    name='SshKeyKiller',
    version='0.0.1',
    packages=find_packages(),
    url='',
    license='MIT',
    author='Anxk',
    author_email='anxiaokang@hotmail.com',
    description='ssh key killer',
    install_requires=requirements,
)
