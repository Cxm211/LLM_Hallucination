public static <T> T isA(Class<T> clazz) {
        return reportMatcher(new InstanceOf(clazz)).<T>returnNull();
    }

    public static <T> T eq(T value) {
        reportMatcher(new Equals(value));
        return value;
    }  

    public static <T> T same(T value) {
        reportMatcher(new Same(value));
        return value;
    }