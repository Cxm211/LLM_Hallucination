public Object answer(InvocationOnMock invocation) throws Throwable {
        Class<?> returnType = invocation.getMethod().getReturnType();
        if (returnType == Void.TYPE) {
            return null;
        }
        if (returnType.isPrimitive()) {
            if (returnType == Boolean.TYPE) return Boolean.FALSE;
            if (returnType == Byte.TYPE) return Byte.valueOf((byte) 0);
            if (returnType == Short.TYPE) return Short.valueOf((short) 0);
            if (returnType == Integer.TYPE) return Integer.valueOf(0);
            if (returnType == Long.TYPE) return Long.valueOf(0L);
            if (returnType == Float.TYPE) return Float.valueOf(0f);
            if (returnType == Double.TYPE) return Double.valueOf(0d);
            if (returnType == Character.TYPE) return Character.valueOf('\0');
        }
        return null;
    }