package com.example;

import com.google.inject.Binder;
import com.google.inject.Module;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.event.client.EventModule;
import io.airlift.http.server.HttpServerModule;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.json.JsonModule;
import io.airlift.node.NodeModule;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MediaType;

import java.util.function.Supplier;

import static io.airlift.jaxrs.JaxrsBinder.jaxrsBinder;

public class Main
{
    @Path("/hello")
    public class HelloResource
    {
        private final static Supplier<String> DEFAULT_DELEGATE = () -> "Hello, World!";

        private static Supplier<String> delegate = DEFAULT_DELEGATE;

        public static void setDelegate(Supplier<String> delegate)
        {
            HelloResource.delegate = delegate;
        }

        public static void resetDelegate()
        {
            HelloResource.delegate = DEFAULT_DELEGATE;
        }

        @GET
        @Produces(MediaType.TEXT_PLAIN)
        public String hello()
        {
            return HelloResource.delegate.get();
        }
    }

    public class Filter implements ContainerResponseFilter
    {
        private static boolean filterCalled = false;

        @Override
        public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
        {
            filterCalled = true;
            responseContext.getHeaders().add("X-Filter-Applied", "true");
        }

        public static boolean wasFilterCalled()
        {
            return filterCalled;
        }

        public static void resetFilterCalled()
        {
            filterCalled = false;
        }
    }

    public static class ServiceModule
            implements Module
    {
        @Override
        public void configure(Binder binder)
        {
            jaxrsBinder(binder).bind(HelloResource.class);
            jaxrsBinder(binder).bind(Filter.class);
        }
    }

    public static void main(String[] args)
    {
        Bootstrap app = new Bootstrap(new ServiceModule(),
                new NodeModule(),
                new HttpServerModule(),
                new EventModule(),
                new JsonModule(),
                new JaxrsModule());
        app.initialize();
    }
}