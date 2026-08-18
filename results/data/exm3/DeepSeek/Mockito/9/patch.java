    public Object answer(InvocationOnMock invocation) throws Throwable {
        if (java.lang.reflect.Modifier.isAbstract(invocation.getMethod().getModifiers())) {
            Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType.isPrimitive()) {
                if (returnType == boolean.class) return false;
                if (returnType == char.class) return '\0';
                if (returnType == byte.class) return (byte)0;
                if (returnType == short.class) return (short)0;
                if (returnType == int.class) return 0;
                if (returnType == long.class) return 0L;
                if (returnType == float.class) return 0.0f;
                if (returnType == double.class) return 0.0d;
            }
            return null;
        }
        return invocation.callRealMethod();
    }