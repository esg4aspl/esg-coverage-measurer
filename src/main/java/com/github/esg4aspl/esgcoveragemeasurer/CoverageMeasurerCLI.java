package com.github.esg4aspl.esgcoveragemeasurer;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.esg4aspl.esgcoveragemeasurer.esgutils.EsgFlattener;
import com.github.esg4aspl.esgcoveragemeasurer.esgutils.VertexRemover;
import com.github.esg4aspl.esgcoveragemeasurer.eventtuplegenerator.EventTuple;
import com.github.esg4aspl.esgcoveragemeasurer.eventtuplegenerator.EventTupleGenerator;
import com.github.esg4aspl.esgcoveragemeasurer.measurer.CoverageResults;
import com.github.esg4aspl.esgcoveragemeasurer.measurer.Measurer;
import com.github.esg4aspl.esgcoveragemeasurer.measurer.filters.FilterByEventAlphabet;
import com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers.FunctionCallMatcher;
import com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers.InstanceToClassNameMatcher;
import com.github.esg4aspl.seqdiag2esg.converter.FrameConverterFactory;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.ElementType;
import com.github.esg4aspl.seqdiag2esg.entity.sequencediagram.Frame;
import com.github.esg4aspl.seqdiag2esg.parser.FrameParserFactory;
import org.apache.commons.lang3.mutable.MutableInt;
import picocli.CommandLine;
import tr.edu.iyte.esg.model.ESG;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@CommandLine.Command(name = "CoverageMeasurer", mixinStandardHelpOptions = true, version = "1.0.SNAPSHOT",
        description = "Measure coverage of test sequences over ESGs.")
public class CoverageMeasurerCLI implements Callable<Integer> {

    @CommandLine.Option(names = {"--test-sequence"}, description = "Path to test sequence file. Can add multiple options.", help = true)
    List<File> testSequenceFiles = new ArrayList<>();

    @CommandLine.Option(names = {"--tuple-length"}, description = "Tuple length to measure against. Can add multiple options.", help = true)
    Set<Integer> tupleLengths = new HashSet<>();

    @CommandLine.Parameters(index = "0", description = "Path to esg inputs.")
    String esgInputPath;

    @CommandLine.Option(names = {"--ts-filter"}, defaultValue = "none", description = "Filter to be used on test sequences.", help = true)
    String testSequenceFilter = "none";
    @CommandLine.Option(names = {"--ts-matcher"}, defaultValue = "none", description = "Matcher to be used on test sequences.", help = true)
    String testSequenceMatcher = "none";

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss Z")
    Date launchTime = Date.from(Instant.now());

    Map<EventTuple, EventTuple> eventTuplesBySource = new HashMap<>();
    Map<String, List<String>> testSequencesBySources = new HashMap<>();
    List<File> esgFiles = new ArrayList<>();
    String commandLine = "";

    public static void main(String[] args) {
        System.out.println("Hi!");
        String commandLine = String.join(" ", args);
        int exitCode = new CommandLine(new CoverageMeasurerCLI(commandLine)).execute(args);
        System.exit(exitCode);
    }

    public CoverageMeasurerCLI(String commandLine) {
        this.commandLine = commandLine;
    }

    public CoverageMeasurerCLI() {
    }

    public Integer call() throws IOException {
        readEsgFilesAndGenerateEventTuples();
        readTestSequences();
        Measurer measurer = new Measurer(eventTuplesBySource.keySet(), testSequencesBySources);
        if (testSequenceFilter.equals("esg-alphabet")) {
            measurer.setTestSequenceFilter(new FilterByEventAlphabet());
        }
        if (testSequenceMatcher.equals("instance-to-class")) {
            measurer.setMatcher(new InstanceToClassNameMatcher());
        } else if (testSequenceMatcher.equals("function-call")) {
            measurer.setMatcher(new FunctionCallMatcher());
        }
        CoverageResults coverageResults = measurer.calculateCoverage();
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("results", coverageResults);
        output.put("_options", this);
        ObjectMapper objectMapper = new ObjectMapper();
        DateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
        String launchTimeString = df.format(launchTime);
        objectMapper.writeValue(new File("report_" + launchTimeString + ".json"), output);
        return 0;
    }

    private void readTestSequences() throws IOException {
        for (File file : testSequenceFiles) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                MutableInt counter = new MutableInt(1);
                reader.lines().map(line -> line.split(",")).map(Arrays::asList)
                        .forEach(list -> testSequencesBySources.put(file.getName() + ':' + counter.getAndIncrement(), list));
            }
        }
    }

    private void readEsgFilesAndGenerateEventTuples() throws IOException {
        final String PSUEDO_EVENT_PREFIX = "[ESG_PSEUDO]";
        Files.newDirectoryStream(Path.of(esgInputPath)).forEach(path -> esgFiles.add(path.toFile()));
        for (File esgFile : esgFiles) {
            Frame frame;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(esgFile)))) {
                frame = FrameParserFactory.getInstance().getFrameParser(ElementType.SD_FRAME, reader.lines().collect(Collectors.toList())).parseFrame();
            }
            ESG esg = FrameConverterFactory.getInstance().getFrameConverter(ElementType.SD_FRAME, frame, new MutableInt(0), PSUEDO_EVENT_PREFIX).convert();
            EsgFlattener flattener = new EsgFlattener(esg);
            flattener.flatten();
            VertexRemover.removeByPredicate(esg, s -> s.getEvent().getName().startsWith(PSUEDO_EVENT_PREFIX));
            EventTupleGenerator eventTupleGenerator = new EventTupleGenerator(esg, esgFile.getName());
            for (int tupleLength : tupleLengths) {
                Set<EventTuple> eventTuples = eventTupleGenerator.generateTuples(tupleLength);
                for (EventTuple eventTuple : eventTuples) {
                    if (eventTuplesBySource.containsKey(eventTuple)) {
                        eventTuplesBySource.get(eventTuple).addSources(eventTuple.getSources());
                    } else {
                        eventTuplesBySource.put(eventTuple, eventTuple);
                    }
                }
            }
        }
    }

    public List<File> getEsgFiles() {
        return esgFiles;
    }

    public List<File> getTestSequenceFiles() {
        return testSequenceFiles;
    }

    public Set<Integer> getTupleLengths() {
        return tupleLengths;
    }

    public String getEsgInputPath() {
        return esgInputPath;
    }

    public String getTestSequenceFilter() {
        return testSequenceFilter;
    }

    public String getLaunchTime() {
        DateFormat df = new SimpleDateFormat("yyMMdd_HHmmss");
        return df.format(launchTime);
    }

    public String getCommandLine() {
        return commandLine;
    }
}
