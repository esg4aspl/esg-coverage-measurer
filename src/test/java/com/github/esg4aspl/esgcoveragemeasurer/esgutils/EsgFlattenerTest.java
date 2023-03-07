package com.github.esg4aspl.esgcoveragemeasurer.esgutils;

import com.github.esg4aspl.seqdiag2esg.converter.FrameConverterFactory;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import com.github.esg4aspl.seqdiag2esg.parser.FrameParserFactory;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import tr.edu.iyte.esg.model.ESG;
import tr.edu.iyte.esg.model.Edge;
import tr.edu.iyte.esg.model.EdgeSimple;
import tr.edu.iyte.esg.model.EventSimple;
import tr.edu.iyte.esg.model.Vertex;
import tr.edu.iyte.esg.model.VertexRefinedByESG;
import tr.edu.iyte.esg.model.VertexSimple;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class EsgFlattenerTest {

    @Test
    void whenEsgHasSubEsg_shouldMergeIntoParent() throws IOException {

        ESG esg = new ESG(0, "test");
        Vertex entry = new VertexSimple(1, new EventSimple(2, "["));
        Vertex exit = new VertexSimple(1, new EventSimple(2, "]"));
        Vertex v1 = new VertexSimple(1, new EventSimple(2, "A"));
        Vertex v2 = new VertexSimple(1, new EventSimple(2, "B"));

        ESG subEsg = new ESG(-1, "subTest");
        Vertex vRefined = new VertexRefinedByESG(-1, new EventSimple(9, "refined"), subEsg);
        {
            Vertex entryInner = new VertexSimple(1, new EventSimple(2, "["));
            Vertex exitInner = new VertexSimple(1, new EventSimple(2, "]"));
            Vertex v1inner = new VertexSimple(1, new EventSimple(2, "1A"));
            Vertex v2inner = new VertexSimple(1, new EventSimple(2, "1B"));
            Vertex v3inner = new VertexSimple(1, new EventSimple(2, "1C"));
            Vertex v4inner = new VertexSimple(1, new EventSimple(2, "1D"));

            subEsg.addVertex(entryInner);
            subEsg.addVertex(v1inner);
            subEsg.addVertex(v2inner);
            subEsg.addVertex(v3inner);
            subEsg.addVertex(v4inner);
            subEsg.addVertex(exitInner);
            subEsg.addEdge(new EdgeSimple(9, entryInner, v1inner));
            subEsg.addEdge(new EdgeSimple(9, entryInner, v2inner));
            subEsg.addEdge(new EdgeSimple(9, v1inner, v3inner));
            subEsg.addEdge(new EdgeSimple(9, v2inner, v4inner));
            subEsg.addEdge(new EdgeSimple(9, v3inner, exitInner));
            subEsg.addEdge(new EdgeSimple(9, v4inner, exitInner));
        }

        esg.addVertex(entry);
        esg.addVertex(v1);
        esg.addVertex(vRefined);
        esg.addVertex(v2);
        esg.addVertex(exit);
        esg.addEdge(new EdgeSimple(9, entry, v1));
        esg.addEdge(new EdgeSimple(9, v1, vRefined));
        esg.addEdge(new EdgeSimple(9, vRefined, v2));
        esg.addEdge(new EdgeSimple(9, v2, exit));

        EsgFlattener flattener = new EsgFlattener(esg);
        flattener.flatten();
        Assertions.assertEquals(8, esg.getVertexList().size());
        Assertions.assertEquals(8, esg.getEdgeList().size());
    }

    @Test
    void whenMultiplyNestedEsgGiven_shouldFlattenToOneLevel() throws IOException {

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("ESGs/08playerMove.puml").getFile());
        Frame frame;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            frame = FrameParserFactory.getInstance().getFrameParser(ElementType.SD_FRAME, reader.lines().collect(Collectors.toList())).parseFrame();
        }
        ESG esg = FrameConverterFactory.getInstance().getFrameConverter(ElementType.SD_FRAME, frame, new MutableInt(0)).convert();
        EsgFlattener flattener = new EsgFlattener(esg);
        flattener.flatten();
        Assertions.assertEquals(13, esg.getVertexList().size());
        Assertions.assertEquals(16, esg.getEdgeList().size());
    }

    @Test
    void whenEsgIsFlattened_shouldOnlySingleEntryAndExitRemainAndWithNoOrphanVertex() throws IOException {
        // A bug in the flattened was leaving an extra edge to a removed inner entry node

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("ESGs/13stealFromPlayer.puml").getFile());
        Frame frame;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            frame = FrameParserFactory.getInstance().getFrameParser(ElementType.SD_FRAME, reader.lines().collect(Collectors.toList())).parseFrame();
        }
        ESG esg = FrameConverterFactory.getInstance().getFrameConverter(ElementType.SD_FRAME, frame, new MutableInt(0), "[!!!]").convert();
        EsgFlattener flattener = new EsgFlattener(esg);
        flattener.flatten();

        Assertions.assertEquals(1, esg.getVertexList().stream().filter(vertex -> vertex.getEvent().getName().equals("[")).count());
        Assertions.assertEquals(1, esg.getVertexList().stream().filter(vertex -> vertex.getEvent().getName().equals("]")).count());

        Vertex entry = esg.getVertexList().stream().filter(vertex -> vertex.getEvent().getName().equals("[")).findFirst().get();
        Vertex exit = esg.getVertexList().stream().filter(vertex -> vertex.getEvent().getName().equals("]")).findFirst().get();

        // any edge from a [ or to a ] should be to the one and only [ and ] vertices
        Assertions.assertFalse(esg.getEdgeList().stream().filter(edge -> edge.getSource().getEvent().getName().equals("["))
                .anyMatch(edge -> edge.getSource() != entry));
        Assertions.assertFalse(esg.getEdgeList().stream().filter(edge -> edge.getSource().getEvent().getName().equals("]"))
                .anyMatch(edge -> edge.getSource() != exit));

        // all edges should be from and to existing vertices
        Assertions.assertTrue(esg.getEdgeList().stream().map(Edge::getSource).allMatch(vertex -> esg.getVertexList().contains(vertex)));
        Assertions.assertTrue(esg.getEdgeList().stream().map(Edge::getTarget).allMatch(vertex -> esg.getVertexList().contains(vertex)));
    }
}
