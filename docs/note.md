1. sshd 端口修改

   ```shell
   # sshd 配置的修改
   vim /etc/ssh/sshd_config # 修改 Port 字段
   # 重启 sshd
   systemctl restart sshd
   ```

2. 用户审计配置步骤

   ```shell
   #1. 设置保存记录的文件
   USER_MONITOR_FILE=/var/log/usermonitor/usermonitor.log
   
   #2. 创建相应的文件
   mkdir -p $(dirname "${USER_MONITOR_FILE}") && \
       touch "${USER_MONITOR_FILE}"
   
   #3. 修改文件的权限
   chown nobody:nobody "${USER_MONITOR_FILE}" && \
       chmod 002 "${USER_MONITOR_FILE}" && \
       chattr +a "${USER_MONITOR_FILE}"
   
   #4 将下面语句加入 /etc/profile
   export PROMPT_COMMAND='{ date "+%y-%m-%d %T %z ## $(who am i |awk "{print \$1\" \"\$2\" \"\$5}") ## $(whoami) ## $(history 1 | { read x cmd; echo "$cmd"; })"; } >> /var/log/usermonitor/usermonitor.log'
   
   #5
   source  /etc/profile
   ```

3. 用户授权公钥定时检测和吊销

   ```shell
   python -m sshmanager
   ```

4. smtp 配置

   阿里云两台虚机，`172.17.212.62` 通外网、`172.17.212.63` 不通外网，社区 `discourse` 论坛和 `jenkins` 实例运行在 `172.17.212.63` 虚机上。

   * 需要在 `172.17.212.62` 虚机上配置端口转发规则：

   ```shell
   nohup /usr/local/go-tcp-proxy_1.0.0_linux_amd64 -l "172.17.212.62:465" -r "smtp.exmail.qq.com:587" > /tmp/proxy.465.log 2>&1 &
   nohup /usr/local/go-tcp-proxy_1.0.0_linux_amd64 -l "172.17.212.62:466" -r "smtp.exmail.qq.com:465" > /tmp/proxy.465.log 2>&1 &
   ```

   * `jenkins` 中设置 `smtp` 服务器端口为 `466`，并将 `172.17.212.62 smtp.exmail.qq.com` 追加到 `/etc/hosts` 文件中；
   * `discourse app.yml` 中设置 `smtp` 服务器端口为 `465`，并将 `172.17.212.62 smtp.exmail.qq.com` 追加到 `/etc/hosts` 文件中；

