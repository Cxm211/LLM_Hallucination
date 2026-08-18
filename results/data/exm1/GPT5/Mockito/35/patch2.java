public static <T> T same(T value) {
        Class<?> cls = (value == null) ? Object.class : value.getClass();
        return reportMatcher(new Same(value)).<T>returnFor((Class<T>) cls);
    }