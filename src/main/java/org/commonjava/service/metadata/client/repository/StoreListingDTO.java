package org.commonjava.service.metadata.client.repository;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreListingDTO<T extends ArtifactStore>
{
    public List<T> items;
}
