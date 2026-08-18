    public boolean hasSameMethod(Invocation candidate) {        
        //not using method.equals() for 1 good reason:
        //sometimes java generates forwarding methods when generics are in play see JavaGenericsForwardingMethodsTest
        Method m1 = invocation.getMethod();
        Method m2 = candidate.getMethod();
        
        if (m1.equals(m2)) {
            return true;
        }
        if (!m1.getName().equals(m2.getName())) {
            return false;
        }
        Class<?>[] params1 = m1.getParameterTypes();
        Class<?>[] params2 = m2.getParameterTypes();
        if (params1.length != params2.length) {
            return false;
        }
        for (int i = 0; i < params1.length; i++) {
            if (!params1[i].equals(params2[i])) {
                return false;
            }
        }
        Class<?> c1 = m1.getDeclaringClass();
        Class<?> c2 = m2.getDeclaringClass();
        if (c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1)) {
            return true;
        }
        return false;
    }