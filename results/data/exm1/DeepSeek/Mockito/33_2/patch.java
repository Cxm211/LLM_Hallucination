public boolean hasSameMethod(Invocation candidate) {        
        //not using method.equals() for 1 good reason:
        //sometimes java generates forwarding methods when generics are in play see JavaGenericsForwardingMethodsTest
        Method m1 = invocation.getMethod();
        Method m2 = candidate.getMethod();
        
        	/* Avoid unnecessary cloning */
        if (!m1.getName().equals(m2.getName())) {
            return false;
        }
        if (!java.util.Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes())) {
            return false;
        }
        return true;
    }