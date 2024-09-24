package com.example;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

public class Filter
        implements ContainerResponseFilter
{
    private static boolean filterCalled;

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
