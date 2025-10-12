要在代码中使用上述配置的 Resilience4j，你需要将其集成到 Spring Boot 应用中。以下是一个详细的案例，展示如何在代码中使用 Resilience4j 断路器、重试、限流和超时功能。

### 1. 添加依赖

首先，在 `pom.xml` 文件中添加必要的依赖：

```xml
<dependencies>
    <!-- Resilience4j dependencies -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot2</artifactId>
        <version>1.7.1</version>
    </dependency>
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-circuitbreaker</artifactId>
        <version>1.7.1</version>
    </dependency>
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-retry</artifactId>
        <version>1.7.1</version>
    </dependency>
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-ratelimiter</artifactId>
        <version>1.7.1</version>
    </dependency>
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-timelimiter</artifactId>
        <version>1.7.1</version>
    </dependency>
</dependencies>
```

### 2. 配置 `application.yml`

已经在之前提供了详细的 `application.yml` 配置。

### 3. 在代码中使用

接下来，在你的服务类中注入并使用这些功能。

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class BackendService {

    @CircuitBreaker(name = "backendA", fallbackMethod = "fallback")
    @Retry(name = "backendA")
    @RateLimiter(name = "backendA")
    @TimeLimiter(name = "backendA")
    public CompletableFuture<String> doSomething() {
        // 模拟远程调用
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 模拟延迟
                TimeUnit.SECONDS.sleep(1);
                // 模拟成功响应
                return "Hello, Resilience4j!";
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<String> fallback(Throwable t) {
        // 断路器打开时的回退方法
        return CompletableFuture.completedFuture("Fallback response");
    }
}
```

### 4. 使用服务

在你的控制器或其他服务类中调用 `BackendService`：

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BackendController {

    @Autowired
    private BackendService backendService;

    @GetMapping("/do-something")
    public CompletableFuture<String> doSomething() {
        return backendService.doSomething();
    }
}
```

### 详细解释

1. **注解**：
    - `@CircuitBreaker`: 用于指定断路器的名称和回退方法。
    - `@Retry`: 用于指定重试的配置名称。
    - `@RateLimiter`: 用于指定限流器的配置名称。
    - `@TimeLimiter`: 用于指定超时的配置名称。

2. **方法**：
    - `doSomething()`: 模拟一个需要容错处理的远程调用。
    - `fallback(Throwable t)`: 当断路器打开时调用的回退方法。

通过上述配置和代码实现，你可以在 Spring Boot 应用中使用 Resilience4j 提供的各种容错功能，从而提高系统的稳定性和健壮性。