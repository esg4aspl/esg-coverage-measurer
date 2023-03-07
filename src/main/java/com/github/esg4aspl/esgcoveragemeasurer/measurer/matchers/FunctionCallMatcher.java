package com.github.esg4aspl.esgcoveragemeasurer.measurer.matchers;

public class FunctionCallMatcher implements EsgEventToTraceEventMatcher {
    @Override
    public boolean doesMatch(String s, String s2) {
        return getFunctionName(s).equals(getFunctionName(s2));
    }

    String getFunctionName(String s) {
        int left = s.indexOf('.');
        if (left == -1) {
            left = 0;
        } else {
            left = left + 1;
        }

        int right = s.lastIndexOf('(');
        if (right == -1) {
            right = s.length();
        }

        return s.substring(left, right);
    }
}
