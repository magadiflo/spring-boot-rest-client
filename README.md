# Rest Client en Spring Boot 3.2

**Contenido**

- **Capítulo 1.**
  [Un primer vistazo al nuevo Rest Client en Spring Boot 3.2](https://www.youtube.com/watch?v=UDNrJAvKc0k&t=411s)
- **Capítulo 2.**
  [Spring Boot Rest Client: cómo probar llamadas HTTP usando @RestClientTest](https://www.youtube.com/watch?v=jhhi03AIin4&t=632s)

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

---

# Capítulo 2

# [Spring Boot Rest Client: cómo probar llamadas HTTP usando @RestClientTest](https://www.youtube.com/watch?v=jhhi03AIin4&t=632s)

Tutorial tomado del canal de **youtube de Dan Vega**.

En este tutorial, aprenderá cómo probar las llamadas de `Rest Client` en `Spring Boot` usando la anotación
`@RestClientTest`.

---

Recordemos que en el **capítulo 1** habíamos creado la clase de servicio **PostService** que está usando el
`RestClient`, pues en este capítulo vamos a probar dicho servicio.

Para la realización de este test, podríamos usar directamente la clase de servicio `PostService` quien usa en su
interior `RestClient`. Al hacerlo, utilizaríamos el `RestClient` real para hacer la llamada al endpoint público.
Eso está bien, puede ser exactamente lo que estamos tratando de hacer.

En nuestro caso, queremos escribir una prueba que no asuma que el endpoint está disponible, es decir, puede que esté
inactivo, o tal vez aún no se ha implementado.

En resumen, solo queremos escribir una prueba en la que mockearemos el servicio para que el `RestClient` no haga las
llamadas reales, sino más bien, simulemos dichas llamadas configurándole los datos que esperamos recibir.

````java

@RestClientTest(PostService.class)
class PostServiceTest {

    @Autowired
    MockRestServiceServer server;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    PostService postService;

    @Test
    void findAllPosts() throws JsonProcessingException {
        // given
        List<Post> data = List.of(
                new Post(1, 1, "Hello, World!", "This is my first posts!"),
                new Post(2, 1, "Testing Rest Client with @RestClientTest", "This is a post"));

        // when
        // Cada vez que se llame a esta url en particular, mockearemos la respuesta con nuestros propios datos
        this.server.expect(MockRestRequestMatchers.requestTo("https://jsonplaceholder.typicode.com/posts"))
                .andRespond(MockRestResponseCreators.withSuccess(this.objectMapper.writeValueAsString(data), MediaType.APPLICATION_JSON));
        List<Post> posts = this.postService.findAllPosts();

        // then
        Assertions.assertThat(posts.size()).isEqualTo(2);
    }
}
````

**DONDE**

- `@RestClientTest(PostService.class)`, dentro de la anotación colocamos la clase que vamos a probar. La
  anotación `@RestClientTest`, es una anotación para una prueba de cliente Spring Rest que se centra solo en beans que
  usan  `RestTemplateBuilder` o `RestClient.Builder`.  
  El uso de esta anotación deshabilitará la configuración automática completa y, en su lugar, aplicará solo la
  configuración relevante para las pruebas del cliente rest **(es decir, la configuración automática de Jackson o
  GSON y los beans @JsonComponent, pero no los beans @Component normales).**
  De forma predeterminada, las pruebas anotadas con `RestClientTest` también configurarán automáticamente un
  `MockRestServiceServer`.


- `MockRestServiceServer`, punto de entrada principal para las pruebas REST del lado del cliente. Se utiliza para
  pruebas que implican el uso directo o indirecto de RestTemplate. **Proporciona una forma de configurar las solicitudes
  esperadas que se realizarán a través de RestTemplate, así como respuestas simuladas para
  enviar, `eliminando así la necesidad de un servidor real`.**

## Probando test

Podemos ejecutar el test utilizando el mismo IDE de IntelliJ IDEA, pero en mi caso lo haré usando la línea de comando:

````bash
$ M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\15.dan_vega\spring-boot-rest-client (main -> origin)
λ mvn test
[INFO] Scanning for projects...
[INFO]
[INFO] ---------------< dev.magadiflo:spring-boot-rest-client >----------------
[INFO]....
..........

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.4)

2024-04-14T12:59:22.748-05:00  INFO 112 --- [spring-boot-rest-client] [           main] d.m.restclient.app.post.PostServiceTest  : Starting PostServiceTest using Java 21.0.1 with PID 112 (started by USUARIO in M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\15.dan_vega\spring-boot-rest-client)
2024-04-14T12:59:22.751-05:00  INFO 112 --- [spring-boot-rest-client] [           main] d.m.restclient.app.post.PostServiceTest  : No active profile set, falling back to 1 default profile: "default"
2024-04-14T12:59:23.834-05:00  INFO 112 --- [spring-boot-rest-client] [           main] d.m.restclient.app.post.PostServiceTest  : Started PostServiceTest in 1.899 seconds (process running for 3.855)
WARNING: A Java agent has been loaded dynamically (C:\Users\USUARIO\.m2\repository\net\bytebuddy\byte-buddy-agent\1.14.12\byte-buddy-agent-1.14.12.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.435 s -- in dev.magadiflo.restclient.app.post.PostServiceTest
[INFO]
[INFO] Results:
[INFO]
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  12.505 s
[INFO] Finished at: 2024-04-14T12:59:25-05:00
[INFO] ------------------------------------------------------------------------
````

