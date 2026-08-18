public boolean hasSameMethod(Invocation candidate) {
    Method m1 = invocation.getMethod();
    Method m2 = candidate.getMethod();
    
    if (!m1.getName().equals(m2.getName())) {
        return false;
    }
    
    return java.util.Arrays.equals(m1.getParameterTypes(), m2.getParameterTypes());
}