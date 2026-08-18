public Object answer(InvocationOnMock invocation) throws Throwable {
        try {
            return invocation.callRealMethod();
        } catch (Throwable t) {
            Class<?> returnType = invocation.getMethod().getReturnType();
            if (!returnType.isPrimitive()) {
                return null;
            }
            if (returnType == Void.TYPE) return null;
            if (returnType == Boolean.TYPE) return false;
            if (returnType == Character.TYPE) return '\0';
            if (returnType == Byte.TYPE) return (byte) 0;
            if (returnType == Short.TYPE) return (short) 0;
            if (returnType == Integer.TYPE) return 0;
            if (returnType == Long.TYPE) return 0L;
            if (returnType == Float.TYPE) return 0f;
            if (returnType == Double.TYPE) return 0d;
            return null;
        }
    }