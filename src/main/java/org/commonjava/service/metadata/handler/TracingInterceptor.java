package org.commonjava.service.metadata.handler;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Traced
@Priority(10)
@Interceptor
public class TracingInterceptor
{

    private final Logger logger = LoggerFactory.getLogger( getClass() );

    @Inject
    OtelAdapter otel;

    @AroundInvoke
    Object logInvocation( InvocationContext context ) throws Exception {

        Scope scope = null;

        Span span = otel.newClientSpan(context.getMethod().getName(), context.getMethod().getName());

        Object ret;
        try
        {
            if ( span != null )
            {
                scope = span.makeCurrent();
            }
            ret = context.proceed();
        }
        catch (Exception e)
        {
            if ( span != null )
            {
                span.setStatus( StatusCode.ERROR );
                span.recordException( e );
            }
            throw e;
        }
        finally
        {
            if ( span != null )
            {
                scope.close();
                span.end();
            }
        }

        return ret;
    }

}
