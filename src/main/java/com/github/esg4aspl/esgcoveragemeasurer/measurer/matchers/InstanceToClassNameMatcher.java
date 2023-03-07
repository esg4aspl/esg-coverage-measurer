package com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class InstanceToClassNameMatcher implements EsgEventToTraceEventMatcher {
    @Override
    public boolean doesMatch(String esgEvent, String traceEvent) {
        esgEvent = removeParameters(esgEvent);
        traceEvent = removeParameters(traceEvent);
        String[] esgSplit = esgEvent.split("\\.");
        String[] traceSplit = traceEvent.split("\\.");
        if (esgSplit.length != 2 || traceSplit.length != 2) {
            return false;
        }
        if (!esgSplit[1].equals(traceSplit[1])) {
            return false;
        }
        String esgClassName = replaceInstanceNameWithClassName(esgSplit[0]);
        String traceClassName = replaceInstanceNameWithClassName(traceSplit[0]);
        if (esgClassName.equals(traceClassName)) {
            return true;
        }
        Set<String> superClassesOfTraceClass = getInheritancesOfClassName().get(traceClassName);
        if (superClassesOfTraceClass == null) {
            return false;
        }
        return superClassesOfTraceClass.contains(esgClassName);
    }

    String replaceInstanceNameWithClassName(String classOrInstanceName) {
        if (!getInstanceNameToClassName().containsKey(classOrInstanceName)) {
            return classOrInstanceName;
        }
        // replace instanceName with found className
        return getInstanceNameToClassName().get(classOrInstanceName);
    }

    String removeParameters(String s) {
        int right = s.lastIndexOf('(');
        if (right == -1) {
            // no parameter
            return s;
        }
        return s.substring(0, right);
    }

    private Map<String, String> instanceNameToClassName = Collections.emptyMap();
    private Map<String, Set<String>> inheritancesOfClassName = new HashMap<>();

    public InstanceToClassNameMatcher() {
        // TODO: read filenames from config
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, String>> mapTypeReference = new TypeReference<Map<String, String>>() {
        };
        File file = new File("src/test/resources/ESGs/instanceNameToClassNameMapping.json");
        if (file.exists()) {
            try {
                instanceNameToClassName = mapper.readValue(new File("src/test/resources/ESGs/instanceNameToClassNameMapping.json"), mapTypeReference);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        TypeReference<Map<String, List<String>>> mapStringToListOfStringTypeReference = new TypeReference<Map<String, List<String>>>() {
        };
        file = new File("src/test/resources/ESGs/instanceNameToClassNameMapping.json");
        if (file.exists()) {
            Map<String, List<String>> inheritances;
            try {
                inheritances = mapper.readValue(new File("src/test/resources/ESGs/inheritances.json"), mapStringToListOfStringTypeReference);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // lazy implementation of a topological-sort traversal over non-cyclic class inheritance graph
            Set<String> classesWithParent = inheritances.keySet();
            Set<String> allClassNames = inheritances.values().stream().flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            allClassNames.addAll(classesWithParent);
            Set<String> classesWithNoParent = allClassNames.stream().filter(s -> !classesWithParent.contains(s)).collect(Collectors.toSet());
            classesWithParent.forEach(cls -> inheritancesOfClassName.put(cls, new HashSet<>(inheritances.get(cls))));
            classesWithNoParent.forEach(cls -> inheritancesOfClassName.put(cls, new HashSet<>()));
            Set<String> notVisitedClasses = new HashSet<>(allClassNames);
            while (!notVisitedClasses.isEmpty()) {
                String visiting = notVisitedClasses.iterator().next();
                dfsInheritanceHelper(visiting, notVisitedClasses);
            }
        }
    }

    void dfsInheritanceHelper(String current, Set<String> notVisitedClasses) {
        notVisitedClasses.remove(current);
        Set<String> subclasses = inheritancesOfClassName.get(current);
        // collect subclasses' subclasses first
        subclasses.forEach(subclass -> dfsInheritanceHelper(subclass, notVisitedClasses));
        // then collect their subclasses on yourself
        Set<String> subsubclasses = subclasses.stream().map(subclass -> inheritancesOfClassName.get(subclass)).flatMap(strings -> strings.stream()).collect(Collectors.toSet());
        subclasses.addAll(subsubclasses);
    }

    public Map<String, String> getInstanceNameToClassName() {
        return instanceNameToClassName;
    }

    public Map<String, Set<String>> getInheritancesOfClassName() {
        return inheritancesOfClassName;
    }
}
