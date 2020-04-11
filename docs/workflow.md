

## 运维文档

### Nginx 部署和维护

Nginx 采用容器部署，配置更新流程如下：

​	`[PR] -> [配置格式校验和 review] -> [Jenkins Job 触发 Nginx 配置热更新] -> [邮件通知]`

备注：对应流水线位于`<repo>/xxx/nginx.groovy 和 <repo>/xxx/pipeline.config`。

### sshd 和用户管理

* 服务端的端口修改为非默认端口

* 公钥和账号管理
  * 运维用户采用公共账号 ops
  * 添加公钥（`ssh-copy-id`）
  * 使用 ssh-key-killer 每天检查和吊销授权
  * 公钥添加和授权流程：
    `[修改授权期限配置并提交 PR] -> [配置格式校验和 review] -> [Jenkins Job 触发 授权配置更新并立即执行一次授权公钥的更新]`

* 用户审计
  记录用户 ip 和 操作记录
