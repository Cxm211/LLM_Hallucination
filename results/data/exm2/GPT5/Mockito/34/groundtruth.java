// ===== FIXED org.mockito.internal.invocation.InvocationMatcher :: captureArgumentsFrom(Invocation) [lines 103-111] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-34-fixed/src/org/mockito/internal/invocation/InvocationMatcher.java =====
    public void captureArgumentsFrom(Invocation i) {
        int k = 0;
        for (Matcher m : matchers) {
            if (m instanceof CapturesArguments && i.getArguments().length > k) {
                ((CapturesArguments) m).captureFrom(i.getArguments()[k]);
            }
            k++;
        }
    }
