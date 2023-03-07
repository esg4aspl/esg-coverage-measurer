package com.github.esg4aspl.esgcoveragemeasurer.measurer;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.esg4aspl.esgcoveragemeasurer.eventtuplegenerator.EventTuple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CoverageResults {

    Map<Integer, TupleLengthResult> resultPerTupleLength = new HashMap<>();

    void insertCoveredTuple(EventTuple tuple) {
        int tupleLength = tuple.getEvents().size();
        resultPerTupleLength.computeIfAbsent(tupleLength, TupleLengthResult::new).insertCoveredTuple(tuple);
    }

    void insertNotCoveredTuple(EventTuple tuple) {
        int tupleLength = tuple.getEvents().size();
        resultPerTupleLength.computeIfAbsent(tupleLength, TupleLengthResult::new).insertNotCoveredTuple(tuple);
    }

    public Map<Integer, TupleLengthResult> getResultPerTupleLength() {
        return resultPerTupleLength;
    }
}

@JsonPropertyOrder({"tupleLength", "coverage", "coveredTupleCount", "notCoveredTupleCount", "totalTupleCount"})
class TupleLengthResult {
    private int tupleLength;

    private Set<EventTuple> coveredTuples = new HashSet<>();
    private Set<EventTuple> notCoveredTuples = new HashSet<>();

    public int getTupleLength() {
        return tupleLength;
    }

    public Set<EventTuple> getCoveredTuples() {
        return coveredTuples;
    }

    public Set<EventTuple> getNotCoveredTuples() {
        return notCoveredTuples;
    }

    public TupleLengthResult(int tupleLength) {
        this.tupleLength = tupleLength;
    }

    public void insertCoveredTuple(EventTuple tuple) {
        coveredTuples.add(tuple);
    }

    public void insertNotCoveredTuple(EventTuple tuple) {
        notCoveredTuples.add(tuple);
    }

    public int getTotalTupleCount() {
        return getCoveredTupleCount() + getNotCoveredTupleCount();
    }

    public int getCoveredTupleCount() {
        return coveredTuples.size();
    }

    public int getNotCoveredTupleCount() {
        return notCoveredTuples.size();
    }

    public float getCoverage() {
        return ((float) getCoveredTupleCount()) / getTotalTupleCount();
    }
}
