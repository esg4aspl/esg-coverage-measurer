package com.github.esg4aspl.esgcoveragemeasurer.measurer.filters;

import com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers.EsgEventToTraceEventMatcher;
import org.apache.commons.lang3.function.TriFunction;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterByEventAlphabet implements TriFunction<Set<String>, List<String>, EsgEventToTraceEventMatcher, List<String>> {

    @Override
    public List<String> apply(Set<String> alphabet, List<String> eventSequence, EsgEventToTraceEventMatcher matcher) {
        return eventSequence.stream().filter(eventInSequence -> alphabet.stream().anyMatch(letter -> matcher.doesMatch(letter, eventInSequence)))
                .collect(Collectors.toList());
    }

}
