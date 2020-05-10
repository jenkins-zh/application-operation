# ssh-manager

## install & uninstall
* install
    ```shell script
    cd ssh-manager && pip3 install .
    ```
* uninstall
    ```shell script
    pip3 uninstall sshmanager
    ```

## usage
* simple usage, the config is default to `/etc/ssh-manager/config.yaml`
    ```shell script
    python3 -m sshmanager
    ```
* specifies the config file
    ```shell script
    python3 -m sshmanager --config /tmp/config.yaml
    ```
* verify the ssh public keys config file
    ```shell script
    python3 -m sshmanager --verify
    ```

## other
* log file was at `/tmp/ssh-manager.log`
* public keys config example (`*.yaml`/ `*.yml`)
    ```yaml
    hosts:
      - annotation: anxk # such as github id or something else
        user: ops
        expire_date: 20200701
        pub_key: "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDVDqzIue64dZg9kYdnQiD9WkqXTPZL6HJpMI\
          89oTKZp7AiN5FTp..."
    ```