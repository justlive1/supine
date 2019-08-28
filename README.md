# supine
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/vip.justlive/supine/badge.svg)](https://maven-badges.herokuapp.com/maven-central/vip.justlive/supine/)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

轻量级RPC框架

## 介绍

轻量级去中心的RPC框架

* 使用AIO通讯
* 采用Multicast进行服务注册发现


## 快速开始


创建`Maven`项目

```xml
<dependency>
    <groupId>vip.justlive</groupId>
    <artifactId>supine</artifactId>
    <version>${lastVersion}</version>
</dependency>
```

或`Gradle`

```
compile 'vip.justlive:supine:$lastVersion'
```

服务端

- 直连方式
```java

ServiceConfig config = new ServiceConfig(10086);
ServiceFactory factory = new ServiceFactory(config);

// 注册服务
factory.register(new SayImpl());
factory.register(new SayImpl2(), "2");

factory.start();
// 服务端常驻
factory.sync();
```

- multicast注册
```java
ServiceConfig config = new ServiceConfig(10086);

// 设置注册类型
config.setRegistryType(1);

// 设置注册地址，不设置时使用默认值（234.69.69.69:56969）
config.setRegistryAddress("234.69.69.69:56969");
ServiceFactory factory = new ServiceFactory(config);

// 注册服务
factory.register(new SayImpl());
// 指定服务版本
factory.register(new SayImpl2(), "2");

factory.start();
// 服务端常驻
factory.sync();
```

客户端

- 直连方式
```java
ClientConfig config = new ClientConfig();

// 设置长连接空闲超时时间，默认120秒
config.setIdleTimeout(120);

// 直连时设置服务端地址
config.setRegistryAddress("localhost:10086");

// 是否异步调用，默认为同步
config.setAsync(false);
ReferenceFactory factory = new ReferenceFactory(config);

// 创建接口代理
Say say = factory.create(Say.class);
// 调用接口
String result = say.hello(msg);
```

- multicast方式
```java
ClientConfig config = new ClientConfig();

// 开启异步调用
config.setAsync(true);

// 指定注册地址，不填则使用默认值（234.69.69.69:56969）
config.setRegistryAddress("234.69.69.69:56969");
ReferenceFactory factory = new ReferenceFactory(config);

// 创建指定版本的接口代理
Say say = factory.create(Say.class, "2");
// 调用接口，注意异步方式返回值为null
say.hello(msg);

// 异步方式下需要调用如下方法获取Future
ResultFuture<String> future = ResultFuture.future();

// 设置回调
future.setOnSuccess(System.out::println);
future.setOnFailure(System.out::println);

// 获取结果
future.get();
// 获取结果，有等待超时时间
future.get(1, TimeUnit.SECONDS);

```