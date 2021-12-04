# EasyIdGenerator 生成全局唯一id

- 递增性

  插入数据库能够保证数据顺序写入，不会页分裂，磁盘利用率下降
- 安全

  id不能包含敏感信息，不能被暴露递增规律

## 支持两种方式

- 号段方式：利用mysql的自增功能

```
easy-id-generator:
    segment:
        enable: false/true # 关闭/开启 mysql自增功能
        db-list: ["db1","db2"] # 数据库配置:可以支持多个库，数据库配置文件名字按dbXXX格式，eg:db1,db2,db3....
        fetch-segment-retry-times: 3 # 从数据库获取号段失败重试次数
```

- 雪花算法：workId是通过zk生产的

```
easy-id-generator:
    snowflake:
        enable: false/true # 关闭/开启雪花算法生成id
        zk:
          connection-string: 127.0.0.1:2181          # ip:port,ip2:prort zk链接信息
        load-worker-id-from-file-when-zk-down: true  # 当zk不可访问时，从本地文件中读取之前备份的workerId
```

## 环境配置

- 下载docker
- cmd命令行，cd到docker-compose.yml所在目录，执行docker-compose up -d

## 号段方式

- 在数据库执行schema.sql脚本
- 通过SegmentEasyIdController控制器，获取id

## 雪花算法方式

- 通过SnowflakeEasyIdController控制器，获取id

## 体验

[雪花❄️](http://java.iamzbb.tech:8090/snowflake/ids/next_id/batches?batchSize=10)

[号段☎](http://java.iamzbb.tech:8090/segment/ids/next_id/batches?businessType=order_business&&batchSize=10)

## easy-id-generator-spring-boot-starter

在starter分支

## 作者邮箱

- zbbpoplar@163.com
