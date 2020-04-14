# ssh-key-killer

## install & uninstall
* install
    ```shell script
    cd ssh-key-killer && pip3 install .
    ```
* uninstall
    ```shell script
    pip3 uninstall sshkeykiller
    ```

## usage

* simple usage, the config is default to `/etc/ssh-key-killer/*.yaml`
    ```shell script
    python3 -m SshKeyKiller
    ```
* specifies the config file or config directory
    ```shell script
    # python3 -m SshKeyKiller --config /tmp
    python3 -m SshKeyKiller --config /tmp/test.yaml
    ```
* verify the config
    ```shell script
    python3 -m SshKeyKiller --config /tmp/ --verify
    ```

## other
* log file was at `/var/log/ssh-key-killer.log`
* config example (config file can be `*.yaml` or `*.yml`)
    ```yaml
    hosts:
      - annotation: anxk # such as github id or something else
        expire_date: 20200301
        pub_key: "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDVDqzIue64dZg9kYdnQiD9WkqXTPZL6HJpMI\
          89oTKZp7AiN5FTp/a6F3w4gqmmqO/minmlhy7qjsUiOSvI7CuhoHC7euZmGFgV7SUUA1HpxkTiE4aRxnzv2XK6pqTu67\
          CVb6NB+cCgJcwT9nGsvqGtfg22AfM..."
    ```