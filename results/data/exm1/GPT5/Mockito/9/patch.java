public Object answer(InvocationOnMock invocation) throws Throwable {
        java.lang.reflect.Method method = invocation.getMethod();
        String name = method.getName();
        Object[] args = invocation.getArguments();

        if ("compareTo".equals(name) && args != null && args.length == 1) {
            return (args[0] == invocation.getMock()) ? 0 : 1;
        }

        Class<?> returnType = method.getReturnType();
        if (returnType == Void.TYPE) {
            return null;
        }
        if (returnType == Boolean.TYPE) return false;
        if (returnType == Character.TYPE) return (char) 0;
        if (returnType == Byte.TYPE) return (byte) 0;
        if (returnType == Short.TYPE) return (short) 0;
        if (returnType == Integer.TYPE) return 0;
        if (returnType == Long.TYPE) return 0L;
        if (returnType == Float.TYPE) return 0F;
        if (returnType == Double.TYPE) return 0D;
        if (returnType.isArray()) {
            return java.lang.reflect.Array.newInstance(returnType.getComponentType(), 0);
        }
        return null;
    }