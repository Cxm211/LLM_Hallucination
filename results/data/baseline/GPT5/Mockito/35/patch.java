public static <T> T isA(Class<T> clazz) {
        return reportMatcher(new InstanceOf<T>(clazz)).<T>returnNull();
    }