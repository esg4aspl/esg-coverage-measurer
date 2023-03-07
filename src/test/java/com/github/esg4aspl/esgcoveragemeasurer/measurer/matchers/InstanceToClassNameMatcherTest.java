package com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstanceToClassNameMatcherTest {

    @Test
    void whenClassAndInstanceNamesMatch_shouldReturnTrue(){
        InstanceToClassNameMatcher mockMatcher = Mockito.mock(InstanceToClassNameMatcher.class);

        when(mockMatcher.getInstanceNameToClassName()).thenReturn(Map.of("v2", "Virologist"));
        when(mockMatcher.replaceInstanceNameWithClassName(anyString())).thenCallRealMethod();
        when(mockMatcher.removeParameters(anyString())).thenCallRealMethod();
        when(mockMatcher.doesMatch(anyString(), anyString())).thenCallRealMethod();

        Assertions.assertTrue(mockMatcher.doesMatch("v2.functionName()", "Virologist.functionName"));
    }

    @Test
    void whenDifferentClassNames_thenShouldNotMatch(){
        InstanceToClassNameMatcher mockMatcher = Mockito.mock(InstanceToClassNameMatcher.class);

        when(mockMatcher.getInstanceNameToClassName()).thenReturn(Map.of("foo", "Foo"));
        when(mockMatcher.getInheritancesOfClassName()).thenReturn(Map.of("Foo", Collections.emptySet(), "Bar", Collections.emptySet()));
        when(mockMatcher.replaceInstanceNameWithClassName(anyString())).thenCallRealMethod();
        when(mockMatcher.removeParameters(anyString())).thenCallRealMethod();
        when(mockMatcher.doesMatch(anyString(), anyString())).thenCallRealMethod();

        Assertions.assertFalse(mockMatcher.doesMatch("foo.functionName", "Bar.functionName"));
    }

    @Test
    void whenDifferentClassNamesButTraceIsSubclassOfEsg_thenShouldMatch(){
        InstanceToClassNameMatcher mockMatcher = Mockito.mock(InstanceToClassNameMatcher.class);

        when(mockMatcher.getInstanceNameToClassName()).thenReturn(Map.of("foo", "Foo"));
        when(mockMatcher.getInheritancesOfClassName()).thenReturn(Map.of("Bar", Set.of("Foo"), "Foo", Collections.emptySet()));
        when(mockMatcher.replaceInstanceNameWithClassName(anyString())).thenCallRealMethod();
        when(mockMatcher.removeParameters(anyString())).thenCallRealMethod();
        when(mockMatcher.doesMatch(anyString(), anyString())).thenCallRealMethod();

        Assertions.assertTrue(mockMatcher.doesMatch("foo.functionName", "Bar.functionName"));
    }

    @Test
    void whenDifferentClassNamesButTraceIsSuperclassOfEsg_thenShouldNotMatch(){
        InstanceToClassNameMatcher mockMatcher = Mockito.mock(InstanceToClassNameMatcher.class);

        when(mockMatcher.getInstanceNameToClassName()).thenReturn(Map.of("foo", "Foo"));
        when(mockMatcher.getInheritancesOfClassName()).thenReturn(Map.of("Foo", Set.of("Bar"), "Bar", Collections.emptySet()));
        when(mockMatcher.replaceInstanceNameWithClassName(anyString())).thenCallRealMethod();
        when(mockMatcher.removeParameters(anyString())).thenCallRealMethod();
        when(mockMatcher.doesMatch(anyString(), anyString())).thenCallRealMethod();

        Assertions.assertFalse(mockMatcher.doesMatch("foo.functionName", "Bar.functionName"));
    }
}
