    public static <T> List<T> anyListOf(Class<T> clazz) {
        return (List) reportMatcher(new InstanceOf(List.class)).returnList();
    }