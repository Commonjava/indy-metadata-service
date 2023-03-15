/**
 * Copyright (C) 2021 Red Hat, Inc. (https://github.com/Commonjava/service-parent)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
