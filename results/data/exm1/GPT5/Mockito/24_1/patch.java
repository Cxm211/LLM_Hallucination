public Object answer(InvocationOnMock invocation) {
        if (methodsGuru.isToString(invocation.getMethod())) {
            Object mock = invocation.getMock();
            MockName name = mockUtil.getMockName(mock);
            if (name.isDefault()) {
                return "Mock for " + mockUtil.getMockSettings(mock).getTypeToMock().getSimpleName() + ", hashCode: " + mock.hashCode();
            } else {
                return name.toString();
            }
        } else if (methodsGuru.isCompareToMethod(invocation.getMethod())) {
            Object[] args = invocation.getArguments();
            Object mock = invocation.getMock();
            if (args != null && args.length == 1 && args[0] == mock) {
                return 0;
            }
            return 1;
        }
        
        Class<?> returnType = invocation.getMethod().getReturnType();
        return returnValueFor(returnType);
    }