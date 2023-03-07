package com.github.esg4aspl.esgcoveragemeasurer.esgutils;

import com.github.esg4aspl.esgcoveragemeasurer.esgutils.VertexRemover;
import com.github.esg4aspl.seqdiag2esg.converter.FrameConverterFactory;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import com.github.esg4aspl.seqdiag2esg.parser.FrameParserFactory;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.EdgeSimple;
import tr.edu.iyte.esg.model.EventSimple;
import tr.edu.iyte.esg.model.Vertex;
import tr.edu.iyte.esg.model.VertexSimple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class VertexRemoverTest {

    @Test
    void whenEsgHasEventsStartingWithRemove_shouldRemoveThem() throws IOException {

        ESG esg = new ESG(0, "test");
        Vertex entry = new VertexSimple(1, new EventSimple(1, "["));
        Vertex exit = new VertexSimple(2, new EventSimple(2, "]"));
        Vertex v1 = new VertexSimple(3, new EventSimple(3, "A"));
        Vertex v2 = new VertexSimple(4, new EventSimple(4, "B"));
        Vertex v3 = new VertexSimple(5, new EventSimple(5, "remove1"));
        Vertex v4 = new VertexSimple(6, new EventSimple(6, "remove2"));
        Vertex v5 = new VertexSimple(7, new EventSimple(7, "remove3"));

        esg.addVertex(entry);
        esg.addVertex(v1);
        esg.addVertex(v2);
        esg.addVertex(v3);
        esg.addVertex(v4);
        esg.addVertex(v5);
        esg.addVertex(exit);
        esg.addEdge(new EdgeSimple(9, entry, v1));
        esg.addEdge(new EdgeSimple(9, v1, v2));
        esg.addEdge(new EdgeSimple(9, v2, exit));
        esg.addEdge(new EdgeSimple(9, entry, v3));
        esg.addEdge(new EdgeSimple(9, v3, v4));
        esg.addEdge(new EdgeSimple(9, v4, v5));
        esg.addEdge(new EdgeSimple(9, v5, exit));

        VertexRemover.removeByPredicate(esg, vertex -> vertex.getEvent().getName().startsWith("remove"));

        Assertions.assertEquals(4, esg.getVertexList().size());
        Assertions.assertEquals(4, esg.getEdgeList().size());
    }

    @Test
    void whenEsgHasLoopWithPseudoEvent_RemoveWithPredicateShouldRemoveIt() throws IOException {
        final String PSEUDO_PREFIX = "PSEUDO_EVENT";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("ESGs/03_1myTurn.puml").getFile());
        Frame frame;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            frame = FrameParserFactory.getInstance().getFrameParser(ElementType.SD_FRAME, reader.lines().collect(Collectors.toList())).parseFrame();
        }
        ESG esg = FrameConverterFactory.getInstance().getFrameConverter(ElementType.SD_FRAME, frame, new MutableInt(0), PSEUDO_PREFIX).convert();
        EsgFlattener flattener = new EsgFlattener(esg);
        flattener.flatten();
        VertexRemover.removeByPredicate(esg, vertex -> vertex.getEvent().getName().startsWith(PSEUDO_PREFIX));
        Assertions.assertTrue(esg.getVertexList().stream().map(vertex -> vertex.getEvent().getName()).noneMatch(s -> s.startsWith(PSEUDO_PREFIX)));
        Assertions.assertTrue(esg.getEdgeList().stream().map(edge -> edge.getSource().getEvent().getName()).noneMatch(s -> s.startsWith(PSEUDO_PREFIX)));
        Assertions.assertTrue(esg.getEdgeList().stream().map(edge -> edge.getTarget().getEvent().getName()).noneMatch(s -> s.startsWith(PSEUDO_PREFIX)));
    }

    @Test
    void whenVertexIsRemoved_shouldConnectItsNeighbors() throws IOException {

        ESG esg = new ESG(0, "test");
        Vertex entry = new VertexSimple(1, new EventSimple(1, "["));
        Vertex exit = new VertexSimple(2, new EventSimple(2, "]"));
        Vertex v1 = new VertexSimple(3, new EventSimple(3, "A"));
        Vertex v2 = new VertexSimple(4, new EventSimple(4, "B"));
        Vertex v3 = new VertexSimple(5, new EventSimple(5, "remove1"));
        Vertex v4 = new VertexSimple(6, new EventSimple(6, "remove2"));
        Vertex v5 = new VertexSimple(7, new EventSimple(7, "remove3"));

        esg.addVertex(entry);
        esg.addVertex(v1);
        esg.addVertex(v2);
        esg.addVertex(v3);
        esg.addVertex(v4);
        esg.addVertex(v5);
        esg.addVertex(exit);
        esg.addEdge(new EdgeSimple(9, entry, v1));
        esg.addEdge(new EdgeSimple(9, v1, v3));
        esg.addEdge(new EdgeSimple(9, v3, v4));
        esg.addEdge(new EdgeSimple(9, v4, v5));
        esg.addEdge(new EdgeSimple(9, v5, v2));
        esg.addEdge(new EdgeSimple(9, v2, exit));

        VertexRemover.removeByPredicate(esg, vertex -> vertex.getEvent().getName().startsWith("remove"));

        Assertions.assertEquals(4, esg.getVertexList().size());
        Assertions.assertEquals(3, esg.getEdgeList().size());
    }
}