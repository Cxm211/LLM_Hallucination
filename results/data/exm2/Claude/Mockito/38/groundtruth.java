// ===== FIXED org.mockito.internal.verification.argumentmatching.ArgumentMatchingTool :: toStringEquals(Matcher, Object) [lines 47-49] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-38-fixed/src/org/mockito/internal/verification/argumentmatching/ArgumentMatchingTool.java =====
    private boolean toStringEquals(Matcher m, Object arg) {
        return StringDescription.toString(m).equals(arg == null? "null" : arg.toString());
    }
