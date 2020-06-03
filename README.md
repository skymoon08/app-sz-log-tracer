# app-sz-log-tracer

SZLogTracer 是一个用于分布式系统调用跟踪的组件，通过统一的 `traceId` 将调用链路中的各种网络调用情况以日志的方式记录下来，以达到透视化网络调用的目的。这些日志可用于故障的快速发现，服务治理等。

## 一、背景

在当下的技术架构实施中，统一采用面向服务的分布式架构，通过服务来支撑起一个个应用，而部署在应用中的各种服务通常都是用复杂大规模分布式集群来实现的，同时，这些应用又构建在不同的软件模块上，这些软件模块，有可能是由不同的团队开发，可能使用不同的编程语言来实现、有可能部署了几千台服务器。因此，就需要一些可以帮助理解各个应用的线上调用行为，并可以分析远程调用性能的组件。

为了能够分析应用的线上调用行为以及调用性能，基于 [OpenTracing 规范](http://opentracing.io/documentation/pages/spec.html) 提供了分布式链路跟踪 SZLogTracer 的解决方案。

## 二、功能简介

为了解决在实施大规模微服务架构时的链路跟踪问题，SZLogTracer 提供了以下的能力：

### 2.1 基于 OpenTracing 规范提供分布式链路跟踪解决方案

基于 [OpenTracing 规范](http://opentracing.io/documentation/pages/spec.html) 并扩展其能力提供链路跟踪的解决方案。各个框架或者组件可以基于此实现，通过在各个组件中埋点的方式来提供链路跟踪的能力。


### 2.2 基于 SLF4J MDC 的扩展能力

SLF4J 提供了 MDC（Mapped Diagnostic Contexts）功能，可以支持用户定义和修改日志的输出格式以及内容。SZLogTracer 集成了 SLF4J MDC 功能，方便用户在只简单修改日志配置文件即可输出当前 Tracer 上下文的 `tracerId` 和 `spanId`。


### 2.3 基于 DUBBO FILTER 的扩展能力



## 三、应用接入
 <dependency>
    <groupId>com.wayyue</groupId>
    <artifactId>sz-log-tracer-boot-starter</artifactId>
    <version>1.0.0</version>
 </dependency>


