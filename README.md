# [Un primer vistazo al nuevo Rest Client en Spring Boot 3.2](https://www.youtube.com/watch?v=UDNrJAvKc0k&t=411s)

Tutorial tomado del canal de **youtube de Dan Vega**.

Este es un primer vistazo al nuevo `Rest Client` en `Spring Boot 3.2`. En este tutorial discutiremos qué es un cliente,
cuáles son las diferentes implementaciones de clientes disponibles y cómo comenzar con el nuevo Rest Client en
`Spring Framework 6.1` y `Spring Boot 3.2`.

**Nota**
> `RestClient` viene dentro de la dependencia de `Spring Web`.

**Enlaces relacionados**

- [Documentación Oficial: Rest Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-request-factories)
- [Bouali Ali - micro-services](https://github.com/magadiflo/micro-services.git)
- [Uncle Dave's Code - microservices](https://github.com/magadiflo/microservices.git)

---

## Dependencias

````xml
<!--Spring Boot 3.2.4-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````
