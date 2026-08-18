public boolean hasSameMethod(Invocation candidate) {        
        //not using method.equals() for 1 good reason:
        //sometimes java generates forwarding methods when generics are in play see JavaGenericsForwardingMethodsTest
        Method m1 = invocation.getMethod();
        Method m2 = candidate.getMethod();
        
        // Fast path
        if (m1.equals(m2)) {
            return true;
        }
        // Compare by method signature (name and parameter types) to tolerate bridge/forwarding methods
        if (!m1.getName().equals(m2.getName())) {
            return false;
        }
        Class<?>[] p1 = m1.getParameterTypes();
        Class<?>[] p2 = m2.getParameterTypes();
        if (p1.length != p2.length) {
            return false;
        }
        for (int i = 0; i < p1.length; i++) {
            if (!p1[i].equals(p2[i])) {
                return false;
            }
        }
        return true;
    }