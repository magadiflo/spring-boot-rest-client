package dev.magadiflo.restclient.app.post;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import java.util.List;

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
        this.server.expect(MockRestRequestMatchers.requestTo("https://jsonplaceholder.typicode.com/posts"))
                .andRespond(MockRestResponseCreators.withSuccess(this.objectMapper.writeValueAsString(data), MediaType.APPLICATION_JSON));
        List<Post> posts = this.postService.findAllPosts();

        // then
        Assertions.assertThat(posts.size()).isEqualTo(2);
    }
}