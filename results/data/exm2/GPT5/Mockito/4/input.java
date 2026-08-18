    public void noMoreInteractionsWanted(Invocation undesired, List<VerificationAwareInvocation> invocations) {
        ScenarioPrinter scenarioPrinter = new ScenarioPrinter();
        String scenario = scenarioPrinter.print(invocations);

        throw new NoInteractionsWanted(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + undesired.getMock() + "':",
                undesired.getLocation(),
                scenario
        ));
    }

    public void noMoreInteractionsWantedInOrder(Invocation undesired) {
        throw new VerificationInOrderFailure(join(
                "No interactions wanted here:",
                new LocationImpl(),
                "But found this interaction on mock '" + undesired.getMock() + "':",
                undesired.getLocation()
        ));
    }

    private String exceptionCauseMessageIfAvailable(Exception details) {
        return details.getCause().getMessage();
    }

// trigger testcase
public void can_use_mock_name_even_when_mock_bogus_default_answer_and_when_reporting_no_more_interaction_wanted() throws Exception {
        Invocation invocation_with_bogus_default_answer = new InvocationBuilder().mock(mock(IMethods.class, new Returns(false))).toInvocation();
        new Reporter().noMoreInteractionsWanted(invocation_with_bogus_default_answer, Collections.<VerificationAwareInvocation>emptyList());
    }

public void can_use_print_mock_name_even_when_mock_bogus_default_answer_and_when_reporting_injection_failure() throws Exception {
        IMethods mock_with_bogus_default_answer = mock(IMethods.class, new Returns(false));
        new Reporter().cannotInjectDependency(someField(), mock_with_bogus_default_answer, new Exception());
    }

public void can_use_print_mock_name_even_when_mock_bogus_default_answer_and_when_reporting_no_more_interaction_wanted_in_order() throws Exception {
        Invocation invocation_with_bogus_default_answer = new InvocationBuilder().mock(mock(IMethods.class, new Returns(false))).toInvocation();
        new Reporter().noMoreInteractionsWantedInOrder(invocation_with_bogus_default_answer);
    }

public void should_not_throw_a_ClassCastException() {
        TestMock test = mock(TestMock.class, new Answer() {
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return false;
            }
        });
        test.m1();
        verifyZeroInteractions(test);
    }
