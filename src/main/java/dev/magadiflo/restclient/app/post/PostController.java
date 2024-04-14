package dev.magadiflo.restclient.app.post;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
