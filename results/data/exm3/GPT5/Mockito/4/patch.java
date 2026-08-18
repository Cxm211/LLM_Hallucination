public void noMoreInteractionsWanted(Invocation undesired, List<VerificationAwareInvocation> invocations) {
        ScenarioPrinter scenarioPrinter = new ScenarioPrinter();
        String scenario = scenarioPrinter.print(invocations);

        String mockName;
        try {
            Object mock = undesired.getMock();
            mockName = mock != null ? mock.getClass().getSimpleName() : "null";
        } catch (Throwable t) {
            mockName = "mock";
        }

        throw new NoInteractionsWanted(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + mockName + "':",
                undesired.getLocation(),
                scenario
        ));
    }