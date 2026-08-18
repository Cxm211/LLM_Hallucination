public static <T> T same(T value) {
        return reportMatcher(new Same<T>(value)).<T>returnNull();
    }