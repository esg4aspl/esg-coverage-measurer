package com.github.esg4aspl.esgcoveragemeasurer.esgutils;

import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Edge;
import tr.edu.iyte.esg.model.EdgeSimple;
import tr.edu.iyte.esg.model.Vertex;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VertexRemover {
    public static void removeByPredicate(ESG esg, Function<Vertex, Boolean> predicate) {
        Set<Vertex> verticesToRemove = esg.getVertexList().stream().filter(predicate::apply).collect(Collectors.toSet());

        verticesToRemove.forEach(vertex -> removeVertex(esg, vertex));
    }

    public static void removeVertex(ESG esg, Vertex vertex) {
        Set<Edge> selfLoopEdges = esg.getEdgeList().stream().filter(edge -> edge.getTarget() == vertex && edge.getSource() == vertex).collect(Collectors.toSet());
        Set<Edge> inboundEdges = esg.getEdgeList().stream().filter(edge -> edge.getTarget() == vertex && edge.getSource() != vertex).collect(Collectors.toSet());
        Set<Edge> outgoingEdges = esg.getEdgeList().stream().filter(edge -> edge.getSource() == vertex && edge.getTarget() != vertex).collect(Collectors.toSet());

        for (Vertex ancestorVertex : inboundEdges.stream().map(Edge::getSource).collect(Collectors.toList())) {
            for (Vertex descendantVertex : outgoingEdges.stream().map(Edge::getTarget).collect(Collectors.toList())) {
                esg.addEdge(new EdgeSimple(0, ancestorVertex, descendantVertex));
            }
        }

        selfLoopEdges.forEach(esg::removeEdge);
        inboundEdges.forEach(esg::removeEdge);
        outgoingEdges.forEach(esg::removeEdge);
        esg.removeVertex(vertex);
    }
}
