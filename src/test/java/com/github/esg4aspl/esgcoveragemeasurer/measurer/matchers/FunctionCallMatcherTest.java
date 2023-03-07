package com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FunctionCallMatcherTest {

    @Test
    void whenOneInputHasParametersAndOneDoesNot_shouldMatch(){
        FunctionCallMatcher matcher = new FunctionCallMatcher();

        Assertions.assertTrue(matcher.doesMatch("getPlayers()", "getPlayers"));
    }
}
