    public static <T> Collection<T> anyCollectionOf(Class<T> clazz) {
        return (Collection) reportMatcher(new InstanceOf(Collection.class)).returnList();
    }