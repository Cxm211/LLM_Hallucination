public static <T> T anyObject() {
        return (T) reportMatcher(Any.ANY_NON_NULL).returnNull();
    }