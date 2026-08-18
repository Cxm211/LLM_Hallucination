public static <T> T isA(Class<T> clazz) {
    return reportMatcher(new InstanceOf(clazz, VarargCapture.DO_NOT_CAPTURE)).<T>returnNull();
}