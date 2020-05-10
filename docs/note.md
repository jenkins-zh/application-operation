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

   

