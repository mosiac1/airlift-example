package com.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.function.Supplier;

@Path("/hello")
public class HelloResource
{
    private static final Supplier<String> DEFAULT_DELEGATE = () -> "Hello, World!";

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
