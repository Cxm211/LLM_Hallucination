public boolean hasSameMethod(Invocation candidate) {
        // not using Method.equals() because it considers the declaring class, which breaks
        // when the same signature is obtained via different types in the hierarchy (e.g., generics/bridge methods)
        Method m1 = invocation.getMethod();
        Method m2 = candidate.getMethod();

        if (m1 == m2) {
            return true;
        }
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