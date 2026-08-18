    public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
        reportMatcher(Any.ANY);
        return (Collection<T>) null;
    }
