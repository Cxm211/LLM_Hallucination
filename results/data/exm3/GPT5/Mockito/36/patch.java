public Object callRealMethod() throws Throwable {
        if (realMethod.getDeclaringClass().isInterface()) {
            throw new MockitoException("Cannot call real method on an interface");
        }
        if (java.lang.reflect.Modifier.isAbstract(realMethod.getModifiers())) {
            throw new MockitoException("Cannot call abstract real method");
        }
        return realMethod.invoke(mock, rawArguments);
    }