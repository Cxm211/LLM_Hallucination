    public static <T> T any(Class<T> clazz) {
        reportMatcher(Any.ANY);
        return (T) null;
    }
