public static <T> T eq(T value) {
        Class<?> cls = (value == null) ? Object.class : value.getClass();
        return reportMatcher(new Equals(value)).<T>returnFor((Class<T>) cls);
    }  