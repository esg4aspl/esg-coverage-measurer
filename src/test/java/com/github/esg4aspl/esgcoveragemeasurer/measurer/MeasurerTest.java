package com.github.esg4aspl.esgcoveragemeasurer.measurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.esg4aspl.esgcoveragemeasurer.eventtuplegenerator.EventTuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tr.edu.iyte.esg.model.Event;
import tr.edu.iyte.esg.model.EventSimple;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MeasurerTest {

    @Test
    void whenEsgHasEventsStartingWithRemove_shouldRemoveThem() throws IOException {
        Set<EventTuple> eventTuples = new HashSet<>();
        eventTuples.add(new EventTuple(List.of("A", "B"), "source1"));
        eventTuples.add(new EventTuple(List.of("C", "X"), "source2"));
        eventTuples.add(new EventTuple(List.of("D", "E"), "source3"));
        eventTuples.add(new EventTuple(List.of("B", "C", "D"), "source3"));

        Map<String,List<String>> testSequences = new HashMap<>();
        testSequences.put("s1", List.of("A", "B", "C", "D"));
        testSequences.put("s2", List.of("B", "C", "D", "E", "F"));

        Measurer measurer = new Measurer(eventTuples, testSequences);
        CoverageResults results = measurer.calculateCoverage();

        Assertions.assertEquals(2, results.resultPerTupleLength.size());
        Assertions.assertTrue(results.resultPerTupleLength.containsKey(2));
        Assertions.assertTrue(results.resultPerTupleLength.containsKey(3));
        Assertions.assertEquals(3, results.resultPerTupleLength.get(2).getTotalTupleCount());
        Assertions.assertEquals(2, results.resultPerTupleLength.get(2).getCoveredTupleCount());
        Assertions.assertEquals(1, results.resultPerTupleLength.get(2).getNotCoveredTupleCount());
        Assertions.assertEquals(1, results.resultPerTupleLength.get(3).getCoveredTupleCount());

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File("report.json"), results);

    }
}