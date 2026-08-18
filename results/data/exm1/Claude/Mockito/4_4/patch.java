public void noMoreInteractionsWanted(Invocation undesired, List<VerificationAwareInvocation> invocations) {
        String scenario = "";
        if (invocations.size() > 1) {
            ScenarioPrinter scenarioPrinter = new ScenarioPrinter();
            scenario = scenarioPrinter.print(invocations);
        }

        throw new NoInteractionsWanted(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + undesired.getMock() + "':",
                undesired.getLocation(),
                scenario
        ));
    }