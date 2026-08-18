public static String anyString() {
        return reportMatcher(Any.ANY_NON_NULL).returnString();
    }