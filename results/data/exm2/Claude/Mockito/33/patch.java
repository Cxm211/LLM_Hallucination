public boolean hasSameMethod(Invocation candidate) {
    // not using method.equals() for 1 good reason:
    // sometimes java generates forwarding methods when generics are in play see JavaGenericsForwardingMethodsTest
    Method m1 = invocation.getMethod();
    Method m2 = candidate.getMethod();

    if (m1.equals(m2)) {
        return true;
    }

    // Check if methods are bridge methods with same name and parameter types
    return m1.getName().equals(m2.getName()) && java.util.Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
}