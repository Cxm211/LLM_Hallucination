public static <T> T eq(T value) {
        return reportMatcher(new Equals<T>(value)).<T>returnNull();
    }  