package com.github.esg4aspl.esgcoveragemeasurer.measurer;

import com.github.esg4aspl.esgcoveragemeasurer.eventtuplegenerator.EventTuple;
import com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers.EsgEventToTraceEventMatcher;
import org.apache.commons.lang3.function.TriFunction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Measurer {
    private Set<EventTuple> setOfTuplesToCover;

    private Map<String, List<String>> testSequencesBySource;

    private CoverageResults coverageResults = new CoverageResults();

    private EsgEventToTraceEventMatcher matcher = String::equals;

    // crazy signature!
    private TriFunction<Set<String>, List<String>, EsgEventToTraceEventMatcher, List<String>> testSequenceFilter
            = (event, testSequence, matcher) -> testSequence;

    public Measurer(Set<EventTuple> setOfTuplesToCover, Map<String, List<String>> testSequencesBySource) {
        this.setOfTuplesToCover = setOfTuplesToCover;
        this.testSequencesBySource = testSequencesBySource;
    }

    public void setMatcher(EsgEventToTraceEventMatcher matcher) {
        this.matcher = matcher;
    }

    public void setTestSequenceFilter(TriFunction<Set<String>, List<String>, EsgEventToTraceEventMatcher, List<String>> testSequenceFilter) {
        this.testSequenceFilter = testSequenceFilter;
    }

    public CoverageResults calculateCoverage() {
        // Bad complexity here. Should be ok unless a huge ESG or high tuple length is fed
        Set<String> distinctSources = setOfTuplesToCover.stream().flatMap(eventTuple -> eventTuple.getSources().stream()).collect(Collectors.toSet());
        Map<String, Set<EventTuple>> tuplesGroupedBySources = new HashMap<>();
        for (String source : distinctSources) {
            Set<EventTuple> eventsWithSource = setOfTuplesToCover.stream().filter(eventTuple -> eventTuple.getSources()
                    .contains(source)).collect(Collectors.toSet());
            tuplesGroupedBySources.put(source, eventsWithSource);
        }
        for (Set<EventTuple> setOfTuples : tuplesGroupedBySources.values()) {
            Set<String> eventAlphabet = setOfTuples.stream().flatMap(eventTuple -> eventTuple.getEvents().stream()).distinct().collect(Collectors.toSet());
            for (EventTuple tuple : setOfTuples) {
                List<String> tupleString = tuple.getEvents();
                boolean tupleExistsInTestSequences = false;
                for (Map.Entry<String, List<String>> sourceAndTestSequence : testSequencesBySource.entrySet()) {
                    List<String> filteredTestSequence = testSequenceFilter.apply(eventAlphabet, sourceAndTestSequence.getValue(), matcher);
                    if (isListSublistOf(tupleString, filteredTestSequence)) {
                        tupleExistsInTestSequences = true;
                        tuple.addCoveredBy(sourceAndTestSequence.getKey());
                    }
                }
                if (tupleExistsInTestSequences) {
                    coverageResults.insertCoveredTuple(tuple);
                } else {
                    coverageResults.insertNotCoveredTuple(tuple);
                }
            }
        }
        return coverageResults;
    }

    boolean isListSublistOf(List<String> needle, List<String> hayStack) {
        // shamelessly taken from Collections.indexOfSubList and adapted with custom element equals method
        int sourceSize = hayStack.size();
        int targetSize = needle.size();
        int maxCandidate = sourceSize - targetSize;
        nextCand:
        for (int candidate = 0; candidate <= maxCandidate; candidate++) {
            for (int i = 0, j = candidate; i < targetSize; i++, j++) {
                if (Boolean.FALSE.equals(matcher.doesMatch(needle.get(i), hayStack.get(j)))) {
                    continue nextCand;  // Element mismatch, try next cand
                }
            }
            return true;  // All elements of candidate matched target
        }
        return false;
    }

}
