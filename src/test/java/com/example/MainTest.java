package com.example;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.event.client.EventModule;
import io.airlift.http.server.testing.TestingHttpServer;
import io.airlift.http.server.testing.TestingHttpServerModule;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.json.JsonModule;
import io.airlift.node.testing.TestingNodeModule;
import jakarta.ws.rs.core.UriBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import static org.assertj.core.api.Assertions.assertThat;

public class MainTest
{
    private HttpClient httpClient;
    private TestingHttpServer server;

    public MainTest()
    {
        Bootstrap app = new Bootstrap(ImmutableList.of(
                new ServiceModule(),
                new TestingNodeModule(),
                new EventModule(),
                new TestingHttpServerModule(),
                new JsonModule(),
                new JaxrsModule()));
        Injector injector = app.initialize();
        server = injector.getInstance(TestingHttpServer.class);
        httpClient = HttpClient.newBuilder().build();
    }

    @BeforeEach
    void setUp() {
        HelloResource.resetDelegate();
        Filter.resetFilterCalled();
    }

    @Test
    void testFilter()
            throws IOException, InterruptedException
    {
        HttpResponse<String> resp = httpClient.send(
                HttpRequest.newBuilder().GET().uri(UriBuilder.fromUri(server.getBaseUrl()).path("/hello").build()).build(),
                BodyHandlers.ofString());

        assertThat(resp.statusCode()).isEqualTo(200);
        assertThat(resp.body()).isEqualTo("Hello, World!");
        assertThat(Filter.wasFilterCalled()).isTrue();
        assertThat(resp.headers().firstValue("X-Filter-Applied")).contains("true");
    }

    @Test
    void testFilterWithException()
            throws IOException, InterruptedException
    {
        HelloResource.setDelegate(() -> {
            throw new RuntimeException("test exception");
        });

        HttpResponse<String> resp = httpClient.send(
                HttpRequest.newBuilder().GET().uri(UriBuilder.fromUri(server.getBaseUrl()).path("/hello").build()).build(),
                BodyHandlers.ofString());

        assertThat(resp.statusCode()).isEqualTo(500);
        assertThat(Filter.wasFilterCalled()).isTrue();
        assertThat(resp.headers().firstValue("X-Filter-Applied")).contains("true");
    }
}