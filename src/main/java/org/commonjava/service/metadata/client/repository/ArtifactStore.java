package org.commonjava.service.metadata.client.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.commonjava.service.metadata.model.StoreKey;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtifactStore
{
    public StoreKey key;
}
