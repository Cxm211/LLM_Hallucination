// ===== FIXED org.mockito.exceptions.Reporter :: noMoreInteractionsWanted(Invocation, List) [lines 417-428] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-4-fixed/src/org/mockito/exceptions/Reporter.java =====
    public void noMoreInteractionsWanted(Invocation undesired, List<VerificationAwareInvocation> invocations) {
        ScenarioPrinter scenarioPrinter = new ScenarioPrinter();
        String scenario = scenarioPrinter.print(invocations);

        throw new NoInteractionsWanted(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + safelyGetMockName(undesired.getMock()) + "':",
                undesired.getLocation(),
                scenario
        ));
    }

// ===== FIXED org.mockito.exceptions.Reporter :: noMoreInteractionsWantedInOrder(Invocation) [lines 430-437] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Mockito/Mockito-4-fixed/src/org/mockito/exceptions/Reporter.java =====
    public void noMoreInteractionsWantedInOrder(Invocation undesired) {
        throw new VerificationInOrderFailure(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + safelyGetMockName(undesired.getMock()) + "':",
                undesired.getLocation()
        ));
    }
