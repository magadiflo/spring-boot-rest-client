package dev.magadiflo.restclient.app.post;

public record Post(
        Integer id,
        Integer userId,
        String title,
        String body) {
}
