public Object answer(InvocationOnMock invocation) throws Throwable {
    java.lang.reflect.Method method = invocation.getMethod();
    if (java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
        Class<?> rt = method.getReturnType();
        if (rt == Void.TYPE) return null;
        if (!rt.isPrimitive()) return null;
        if (rt == Boolean.TYPE) return false;
        if (rt == Character.TYPE) return '\0';
        if (rt == Byte.TYPE) return (byte) 0;
        if (rt == Short.TYPE) return (short) 0;
        if (rt == Integer.TYPE) return 0;
        if (rt == Long.TYPE) return 0L;
        if (rt == Float.TYPE) return 0F;
        if (rt == Double.TYPE) return 0D;
        return null;
    }
    return invocation.callRealMethod();
}