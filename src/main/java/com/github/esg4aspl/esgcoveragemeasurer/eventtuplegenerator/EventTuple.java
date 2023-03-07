package com.github.esg4aspl.esgcoveragemeasurer.eventtuplegenerator;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class EventTuple {
    private Set<String> sources = new HashSet<>();

    private Set<String> coveredBy = new HashSet<>();

    private List<String> events = new LinkedList<>();

    public EventTuple(List<String> events, String source){
        this(events);
        sources.add(source);
    }
    public EventTuple(List<String> events) {
        this.events = events;
    }

    public List<String> getEvents() {
        return events;
    }

    public void addSource(String source){
       sources.add(source);
    }

    public void addSources(Collection<String> sources){
        this.sources.addAll(sources);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventTuple that = (EventTuple) o;
        return getEvents().equals(that.getEvents());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEvents());
    }

    public Set<String> getSources() {
        return sources;
    }

    public Set<String> getCoveredBy() {
        return coveredBy;
    }

    public void addCoveredBy(String covers){
        coveredBy.add(covers);
    }
}
