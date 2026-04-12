# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Spring Boot 4.0.5 排球社区后端服务，使用 Java 17 和 Maven 构建。

## Build & Run Commands

```bash
# 构建项目
./mvnw clean package

# 运行应用
./mvnw spring-boot:run

# 运行测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=VolleyballCommunityBackendApplicationTests
```

## Architecture

- **框架**: Spring Boot 4.0.5 (webmvc)
- **数据库**: MySQL (需配置 `spring.datasource.url` 等属性)
- **ORM**: 未显式配置，默认使用 Spring Data JPA
- **工具**: Lombok (自动生成 getter/setter/constructor 等)
- **包名**: `com.volleyball.volleyballcommunitybackend`

## Configuration

数据库配置在 `src/main/resources/application.properties` 中，目前仅包含 `spring.application.name`。需要添加以下配置才能连接 MySQL:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## Test Notes

测试类位于 `src/test/java` 下，使用 `@SpringBootTest` 进行集成测试。
