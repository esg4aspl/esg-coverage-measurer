package com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers;

public interface EsgEventToTraceEventMatcher {
    boolean doesMatch(String esgEvent, String traceEvent);
}
