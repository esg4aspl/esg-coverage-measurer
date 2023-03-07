package com.github.esg4aspl.esgcoveragemeasurer.esgutils;

import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Edge;
import tr.edu.iyte.esg.model.EdgeSimple;
import tr.edu.iyte.esg.model.Vertex;
import tr.edu.iyte.esg.model.VertexRefinedByESG;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EsgFlattener {
    ESG esg;

    public EsgFlattener(ESG esg) {
        this.esg = esg;
    }

    public void flatten() {
        // keep merging subesgs until all levels are merged into a single level
        while(esg.getVertexList().stream().anyMatch(VertexRefinedByESG.class::isInstance)){
            mergeSubEsgsOnTopLevel();
        }
    }

    public void mergeSubEsgsOnTopLevel() {
        List<VertexRefinedByESG> refinedVertices = esg.getVertexList().stream()
                .filter(VertexRefinedByESG.class::isInstance).map(VertexRefinedByESG.class::cast)
                .collect(Collectors.toList());

        for (VertexRefinedByESG vertexRefinedByESG : refinedVertices) {
            ESG subESG = vertexRefinedByESG.getSubESG();
            List<Edge> edgesToVertex = esg.getEdgeList().stream()
                    .filter(edge -> edge.getTarget().equals(vertexRefinedByESG)).collect(Collectors.toList());
            List<Edge> edgesFromVertex = esg.getEdgeList().stream()
                    .filter(edge -> edge.getSource().equals(vertexRefinedByESG)).collect(Collectors.toList());

            Set<Vertex> outsideVerticesToSuper = edgesToVertex.stream().map(Edge::getSource).collect(Collectors.toSet());
            Set<Vertex> outsideVerticesFromSuper = edgesFromVertex.stream().map(Edge::getTarget).collect(Collectors.toSet());

            List<Edge> edgesFromSuperEntry = subESG.getEdgeList().stream()
                    .filter(edge -> edge.getSource().equals(subESG.getPseudoStartVertex())).collect(Collectors.toList());
            List<Edge> edgesToSuperExit = subESG.getEdgeList().stream()
                    .filter(edge -> edge.getTarget().equals(subESG.getPseudoEndVertex())).collect(Collectors.toList());

            Set<Vertex> subEsgPseudoVertices = Set.of(subESG.getPseudoStartVertex(), subESG.getPseudoEndVertex());

            Set<Vertex> verticesOnSuperEntry = edgesFromSuperEntry.stream().map(Edge::getTarget)
                    .filter(vertex -> !subEsgPseudoVertices.contains(vertex)).collect(Collectors.toSet());
            Set<Vertex> verticesOnSuperExit = edgesToSuperExit.stream().map(Edge::getSource)
                    .filter(vertex -> !subEsgPseudoVertices.contains(vertex)).collect(Collectors.toSet());

            // add all non-pseudo vertices of subesg to parent
            subESG.getVertexList().stream().filter(vertex -> !subEsgPseudoVertices.contains(vertex))
                    .forEach(vertex -> esg.addVertex(vertex));
            // add all non-pseudo vertices of subesg to parent
            subESG.getEdgeList().stream().filter(edge -> !subEsgPseudoVertices.contains(edge.getSource()))
                    .filter(edge -> !subEsgPseudoVertices.contains(edge.getTarget())).forEach(edge -> esg.addEdge(edge));
            // add powersets of edges for vertices in borders
            outsideVerticesToSuper.forEach(outsideVertex ->
                    verticesOnSuperEntry.forEach(insideVertex -> esg.addEdge(new EdgeSimple(0, outsideVertex, insideVertex)))
            );
            outsideVerticesFromSuper.forEach(outsideVertex ->
                    verticesOnSuperExit.forEach(insideVertex -> esg.addEdge(new EdgeSimple(0, insideVertex, outsideVertex)))
            );

            edgesFromSuperEntry.forEach(esg::removeEdge);
            edgesToSuperExit.forEach(esg::removeEdge);
            edgesFromVertex.forEach(esg::removeEdge);
            edgesToVertex.forEach(esg::removeEdge);
            esg.removeVertex(vertexRefinedByESG);

        }
    }
}
