public void validate(Answer<?> answer, Invocation invocation) {
        if (answer instanceof ThrowsException) {
            validateException((ThrowsException) answer, invocation);
        }
        
        if (answer instanceof Returns) {
            validateReturnValue((Returns) answer, invocation);
        }
        
        if (answer instanceof DoesNothing) {
            validateDoNothing((DoesNothing) answer, invocation);
        }
        
        if (answer instanceof CallsRealMethods) {
            java.lang.reflect.Method method = invocation.getMethod();
            if (method.getDeclaringClass().isInterface() || java.lang.reflect.Modifier.isAbstract(method.getModifiers())) {
                throw new MockitoException("Cannot call real method on interface or abstract method");
            }
        }
        
    }