package com.github.esg4aspl.esgcoveragemeasurer.eventtuplegenerator;

import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Edge;
import tr.edu.iyte.esg.model.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EventTupleGenerator {
    private ESG esg;

    private String sourceName;

    private Predicate<Vertex> skipVertexPredicate = (vertex -> vertex.getEvent().getName().equals("[") || vertex.getEvent().getName().equals("]"));

    public EventTupleGenerator(ESG esg, String sourceName) {
        this.esg = esg;
        this.sourceName = sourceName;
    }

    public Set<EventTuple> generateTuples(int tupleLength) {
        // TODO: complexity of this method is O(n^tupleLength)
        // it is possible to use a sliding window of past visits
        // to get it done in O(n) with O(n*tupleLength) extra space
        List<List<String>> results = new ArrayList<>();

        Set<Vertex> visited = new HashSet<>();
        Queue<Vertex> bfsQueue = new LinkedList<>();
        bfsQueue.add(esg.getPseudoStartVertex());

        while (!bfsQueue.isEmpty()) {
            Vertex currentVertex = bfsQueue.remove();
            if (visited.contains(currentVertex)) {
                continue;
            }
            visited.add(currentVertex);
            Set<Vertex> neighbors = getNeighborsOf(currentVertex);
            bfsQueue.addAll(neighbors);
            results.addAll(getTuplesStartingFromVertex(currentVertex, tupleLength));
        }
        return results.stream().map(list -> new EventTuple(list, sourceName)).collect(Collectors.toSet());
    }

    List<List<String>> getTuplesStartingFromVertex(Vertex vertex, int tupleLength) {
        // base case
        boolean currentVertexIgnored = skipVertexPredicate.test(vertex);
        if (tupleLength == 1 && !currentVertexIgnored) {
            // do not use List.of here. Need to return a mutable list.
            List<List<String>> singleTuple = new LinkedList<>();
            List<String> singleEvent = new LinkedList<>();
            singleEvent.add(vertex.getEvent().getName());
            singleTuple.add(singleEvent);
            return singleTuple;
        }
        int tupleLengthForChildren = currentVertexIgnored ? tupleLength : tupleLength - 1;
        List<List<String>> results = new ArrayList<>();
        Set<Vertex> neighbors = getNeighborsOf(vertex);
        for (Vertex neighbor : neighbors) {
            List<List<String>> resultsExcludingCurrent = getTuplesStartingFromVertex(neighbor, tupleLengthForChildren);
            for (List<String> eventTuple : resultsExcludingCurrent) {
                if (!currentVertexIgnored) {
                    eventTuple.add(0, vertex.getEvent().getName());
                }
                results.add(eventTuple);
            }
        }
        return results;
    }

    Set<Vertex> getNeighborsOf(Vertex vertex) {
        return esg.getEdgeList().stream().filter(edge -> edge.getSource().equals(vertex))
                .map(Edge::getTarget).collect(Collectors.toSet());
    }

    public void setSkipVertexPredicate(Predicate<Vertex> skipVertexPredicate) {
        this.skipVertexPredicate = skipVertexPredicate;
    }
}
