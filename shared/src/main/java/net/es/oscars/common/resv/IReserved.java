package net.es.oscars.common.resv;

import java.time.Instant;

public interface IReserved<T> {
    String getUrn();
    public ResourceType getResourceType();
    public T getResource();
    public Instant getValidFrom();
    public Instant getValidUntil();

}
