# Rest Client en Spring Boot 3.2

**Contenido**

- **Capítulo 1.**
  [Un primer vistazo al nuevo Rest Client en Spring Boot 3.2](https://www.youtube.com/watch?v=UDNrJAvKc0k&t=411s)

**Enlaces relacionados**

- [Documentación Oficial: Rest Clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html#rest-request-factories)
- [Bouali Ali - micro-services](https://github.com/magadiflo/micro-services.git)
- [Uncle Dave's Code - microservices](https://github.com/magadiflo/microservices.git)

---

# Capítulo 1

# [Un primer vistazo al nuevo Rest Client en Spring Boot 3.2](https://www.youtube.com/watch?v=UDNrJAvKc0k&t=411s)

Tutorial tomado del canal de **youtube de Dan Vega**.

Este es un primer vistazo al nuevo `Rest Client` en `Spring Boot 3.2`. En este tutorial discutiremos qué es un cliente,
cuáles son las diferentes implementaciones de clientes disponibles y cómo comenzar con el nuevo Rest Client en
`Spring Framework 6.1` y `Spring Boot 3.2`.

**Nota**
> `RestClient` viene dentro de la dependencia de `Spring Web`.
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

    @GetMapping(path = "/{id}")
    public ResponseEntity<Post> findPost(@PathVariable Integer id) {
        return ResponseEntity.ok(this.postService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        Post postDB = this.postService.createPost(post);
        URI uri = URI.create("/api/v1/posts/" + postDB.id());
        return ResponseEntity.created(uri).body(postDB);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<Post> updatePost(@RequestBody Post post, @PathVariable Integer id) {
        return ResponseEntity.ok(this.postService.updatePost(post, id));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Integer id) {
        this.postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
}
````

## Service

En nuestra clase de servicio utilizaremos el `RestClient` como cliente http que nos permitirá consumir apis, en nuestro
caso, consumiremos el api de `jsonplaceholder`.

Observar que estamos inyectado por constructor de la clase PostService el `RestClient.Builder` (builder mutable
para crear un RestClient).

En este caso, se espera que `Spring` **inyecte** una instancia de `RestClient.Builder` al crear una instancia de la
clase `PostService` y esto lo hará de manera transparente, es decir, inyectará una instancia de `RestClient.Builder`
por nosotros.

Podríamos haber creado el `RestClient` directamente dentro del constructor de la clase `PostService` de esta manera:
`RestClient.builder().baseUrl("https://jsonplaceholder.typicode.com").build()`, pero como más adelante vamos a
requerir probar esta clase, es que tenemos que hacerlo de esta otra manera, es decir inyectando el `RestClient.Builder`
por constructor.

````java

@Service
public class PostService {

    private final RestClient restClient;

    public PostService(RestClient.Builder builder) {
        this.restClient = builder
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

    public Post findById(Integer id) {
        return this.restClient.get()
                .uri("/posts/{id}", id)
                .retrieve()
                .body(Post.class);
    }

    public Post createPost(Post post) {
        return this.restClient.post()
                .uri("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .body(post)
                .retrieve()
                .body(Post.class);
    }

    public Post updatePost(Post post, Integer id) {
        return this.restClient.put()
                .uri("/posts/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(post)
                .retrieve()
                .body(Post.class);
    }

    public void deletePost(Integer id) {
        this.restClient.delete()
                .uri("/posts/{id}", id)
                .retrieve()
                .toBodilessEntity();
    }
}
````

## Probando endpoints

- Listar Posts

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

- Buscar post por id

````bash
$ curl -v http://localhost:8080/api/v1/posts/5 | jq
>
< HTTP/1.1 200
{
  "id": 5,
  "userId": 1,
  "title": "nesciunt quas odio",
  "body": "repudiandae veniam quaerat sunt sed\nalias aut fugiat sit autem sed est\nvoluptatem omnis possimus esse voluptatibus quis\nest aut tenetur dolor neque"
}
````

- Crear un post

````bash
$ curl -v -X POST -H "Content-Type: application/json" -d "{\"userId\": 1, \"title\": \"Spring Boot con Rest Client\", \"body\": \"Nueva caracteristica\"}" http://localhost:8080/api/v1/posts | jq
>
< HTTP/1.1 201
<
{
  "id": 101,
  "userId": 1,
  "title": "Spring Boot con Rest Client",
  "body": "Nueva caracteristica"
}
````

- Actualizar post

````bash
$ curl -v -X PUT -H "Content-Type: application/json" -d "{\"id\": 1, \"userId\": 1, \"title\": \"Spring Boot con Rest Client\", \"body\": \"Nueva caracteristica\"}" http://localhost:8080/api/v1/posts/1 | jq
< HTTP/1.1 200
<
{
  "id": 1,
  "userId": 1,
  "title": "Spring Boot con Rest Client",
  "body": "Nueva caracteristica"
}
````

- Eliminar post

````bash
$ curl -v -X DELETE http://localhost:8080/api/v1/posts/5 | jq
>
< HTTP/1.1 204
````