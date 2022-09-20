package org.commonjava.service.metadata.handler;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OtelAdapter
{

    @ConfigProperty( name = "quarkus.opentelemetry.enabled" )
    Boolean enabled;

    public boolean enabled()
    {
        return enabled == Boolean.TRUE;
    }

    public Span newClientSpan(String adapterName, String name )
    {
        if ( !enabled )
        {
            return null;
        }

        return GlobalOpenTelemetry.get()
                .getTracer( adapterName )
                .spanBuilder( name )
                .setSpanKind( SpanKind.CLIENT )
                .setAttribute( "service_name", "metadata-service" )
                .startSpan();
    }

}

