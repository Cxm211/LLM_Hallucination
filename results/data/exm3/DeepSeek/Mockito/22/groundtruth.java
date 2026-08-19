// ===== FIXED org.mockito.internal.matchers.Equality :: areEqual(Object, Object) [lines 12-22] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-22-fixed/src/org/mockito/internal/matchers/Equality.java =====
    public static boolean areEqual(Object o1, Object o2) {
        if (o1 == o2 ) {
            return true;
	} else if (o1 == null || o2 == null) {
            return o1 == null && o2 == null;
        } else if (isArray(o1)) {
            return isArray(o2) && areArraysEqual(o1, o2);
        } else {
            return o1.equals(o2);
        }
    }
