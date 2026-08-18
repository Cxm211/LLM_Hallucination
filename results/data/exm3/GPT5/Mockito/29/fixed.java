// ===== FIXED org.mockito.internal.matchers.Same :: describeTo(Description) [lines 26-32] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-29-fixed/src/org/mockito/internal/matchers/Same.java =====
    public void describeTo(Description description) {
        description.appendText("same(");
        appendQuoting(description);
        description.appendText(wanted == null ? "null" : wanted.toString());
        appendQuoting(description);
        description.appendText(")");
    }
