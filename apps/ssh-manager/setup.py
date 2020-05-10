from os import path
from setuptools import setup, find_packages

here = path.abspath(path.dirname(__file__))

with open(path.join(here, 'requirements.txt')) as f:
    requirements = f.readlines()

requirements = [requirement.replace('\n', '') for requirement in requirements]

setup(
    name='sshmanager',
    version='0.1',
    packages=['sshmanager'],
    url='https://github.com/jenkins-zh/application-operation/apps/ssh-manager',
    license='MIT',
    author='Anxk',
    author_email='anxiaokang@hotmail.com',
    description='A tool to manage ssh authorized_keys files',
    install_requires=requirements,
)
