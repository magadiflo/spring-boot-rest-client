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

## Api externa

Vamos a trabajar con la api [https://jsonplaceholder.typicode.com/](https://jsonplaceholder.typicode.com/) que nos
proporciona endpoints listos para consumir. En nuestro caso trabajaremos con el endpoint de `/posts`.

## Post Record

````java
public record Post(
        Integer id,
        Integer userId,
        String title,
        String body) {
}
````

## Controller

Crearemos nuestro propio endpoint en el controlador `PostController`:

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<Post>> findAllPosts() {
        return ResponseEntity.ok(this.postService.findAllPosts());
    }
}
````

## Service

En nuestra clase de servicio utilizaremos el `RestClient` como cliente http que nos permitirá consumir apis, en nuestro
caso, consumiremos el api de `jsonplaceholder`.

````java

@Service
public class PostService {

    private final RestClient restClient;

    public PostService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com")
                .build();
    }

    public List<Post> findAllPosts() {
        return this.restClient.get()
                .uri("/posts")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
    }
}
````

## Probando Listar Posts

````bash
$ curl -v http://localhost:8080/api/v1/posts | jq
>
< HTTP/1.1 200
<
[
  {
    "id": 1,
    "userId": 1,
    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
    "body": "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto"
  },
  {...},
  {
    "id": 100,
    "userId": 10,
    "title": "at nam consequatur ea labore ea harum",
    "body": "cupiditate quo est a modi nesciunt soluta\nipsa voluptas error itaque dicta in\nautem qui minus magnam et distinctio eum\naccusamus ratione error aut"
  }
]
````