---
layout: default
---
# version 1.1.1

## 增强定制路由特性
配置中的sharding节点上添加自定义属性

自定义属性在pinus启动时会被加载到DBInfo对象中，当用户在定制集群路由器时可以通过自定义属性更灵活的进行分库

配置中的region节点的capacity属性可以设置多个范围，范围值之间使用英文半角逗号分隔

## 自动读写分离
给所有的查询接口添加自动读写分离特性，缓存可用时优先查找缓存，存在从库时查询从库，从库未找到再查询主库

## 新增集群查询接口
添加findByPk、findByPkList、findBySql、findByQuery接口，当实体对象保存在全局库中时效果等同于findGlobalXXX接口，当实体对象保存在分片库中时，会查询所有分片并将结果集合并
