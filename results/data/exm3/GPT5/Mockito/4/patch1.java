public void noMoreInteractionsWantedInOrder(Invocation undesired) {
        String mockName;
        try {
            Object mock = undesired.getMock();
            mockName = mock != null ? mock.getClass().getSimpleName() : "null";
        } catch (Throwable t) {
            mockName = "mock";
        }

        throw new VerificationInOrderFailure(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + mockName + "':",
                undesired.getLocation()
        ));
    }