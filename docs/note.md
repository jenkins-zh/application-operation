1. sshd 端口修改

   ```shell
   # sshd 配置的修改
   vim /etc/ssh/sshd_config # 修改 Port 字段
   # 如果开启 SELinux，需要下面修改（getenforce 查看是否启用了 selinux，semanage port -l | grep ssh 查看 selinux 给 ssh 的端口）
   semanage port -a -t ssh_port_t -p tcp 端口
   # 防火墙端口，这个也需要检查和更新
   # 重启 sshd
   ```

2. 用户审计

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
   
   #3 将下面语句加入 /etc/profile
   export PROMPT_COMMAND='{ date "+%y-%m-%d %T %z ## $(who am i |awk "{print \$1\" \"\$2\" \"\$5}") ## $(whoami) ## $(history 1 | { read x cmd; echo "$cmd"; })"; } >> /var/log/usermonitor/usermonitor.log'
   
   #5
   source  /etc/profile
   ```

4. 用户授权公钥定时检测和吊销

   ```shell
   00 03 * * * python -m ssh-key-killer # crontab
   ```

   

