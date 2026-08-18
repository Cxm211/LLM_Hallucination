public Object answer(InvocationOnMock invocation) throws Throwable {
    java.lang.reflect.Method m = invocation.getMethod();
    if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) {
        Class<?> rt = m.getReturnType();
        if (rt == Void.TYPE) return null;
        if (rt == Boolean.TYPE) return Boolean.FALSE;
        if (rt == Character.TYPE) return Character.valueOf('\0');
        if (rt == Byte.TYPE) return Byte.valueOf((byte) 0);
        if (rt == Short.TYPE) return Short.valueOf((short) 0);
        if (rt == Integer.TYPE) return Integer.valueOf(0);
        if (rt == Long.TYPE) return Long.valueOf(0L);
        if (rt == Float.TYPE) return Float.valueOf(0f);
        if (rt == Double.TYPE) return Double.valueOf(0d);
        return null;
    }
    return invocation.callRealMethod();
}