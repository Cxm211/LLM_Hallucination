// ===== FIXED org.mockito.Matchers :: eq(T) [lines 478-480] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-35-fixed/src/org/mockito/Matchers.java =====
    public static <T> T eq(T value) {
        return (T) reportMatcher(new Equals(value)).<T>returnFor((Class) value.getClass());
    }  

// ===== FIXED org.mockito.Matchers :: isA(Class) [lines 361-363] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-35-fixed/src/org/mockito/Matchers.java =====
    public static <T> T isA(Class<T> clazz) {
        return reportMatcher(new InstanceOf(clazz)).<T>returnFor(clazz);
    }

// ===== FIXED org.mockito.Matchers :: same(T) [lines 515-517] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-35-fixed/src/org/mockito/Matchers.java =====
    public static <T> T same(T value) {
        return (T) reportMatcher(new Same(value)).<T>returnFor((Class) value.getClass());
    }
