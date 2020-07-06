# EasyIdGenerator 生成全局唯一id

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
- cmd命令行，cd到docker-compose.yml所在目录，执行docker-compose -d

## 号段方式

- 在数据库执行schema.sql脚本
- 通过SegmentEasyIdController控制器，获取id

## 雪花算法方式

- 通过SnowflakeEasyIdController控制器，获取id