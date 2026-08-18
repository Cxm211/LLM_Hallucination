public static <T> T isA(Class<T> clazz) {
    reportMatcher(new InstanceOf(clazz));
    return null;
}