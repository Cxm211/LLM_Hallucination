public static <T> T isA(Class<T> clazz) {
    reportMatcher(new InstanceOf(clazz));
    Object defaultValue = null;
    if (clazz == Integer.class) {
        defaultValue = Integer.valueOf(0);
    } else if (clazz == Long.class) {
        defaultValue = Long.valueOf(0L);
    } else if (clazz == Short.class) {
        defaultValue = Short.valueOf((short) 0);
    } else if (clazz == Byte.class) {
        defaultValue = Byte.valueOf((byte) 0);
    } else if (clazz == Float.class) {
        defaultValue = Float.valueOf(0f);
    } else if (clazz == Double.class) {
        defaultValue = Double.valueOf(0d);
    } else if (clazz == Character.class) {
        defaultValue = Character.valueOf('\u0000');
    } else if (clazz == Boolean.class) {
        defaultValue = Boolean.FALSE;
    }
    return (T) defaultValue;
}