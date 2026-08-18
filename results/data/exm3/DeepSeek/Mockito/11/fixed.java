// ===== FIXED org.mockito.internal.creation.DelegatingMethod :: equals(Object) [lines 54-64] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-11-fixed/src/org/mockito/internal/creation/DelegatingMethod.java =====
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof DelegatingMethod) {
            DelegatingMethod that = (DelegatingMethod) o;
            return method.equals(that.method);
        } else {
            return method.equals(o);
        }
    }

// ===== FIXED org.mockito.internal.creation.DelegatingMethod :: hashCode() [lines 67-69] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-11-fixed/src/org/mockito/internal/creation/DelegatingMethod.java =====
    public int hashCode() {
        return method.hashCode();
    }
