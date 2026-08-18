// org/mockito/internal/invocation/InvocationMatcherTest.java
@Test
    public void shouldCaptureArgumentsWhenMoreMatchersThanArgs() throws Exception {
        //given
        mock.varargs("hello");
        Invocation invocation = getLastInvocation();
        ArgumentCaptor<String> captor1 = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> captor2 = ArgumentCaptor.forClass(String.class);
        List<Matcher> matchers = asList(
            new LocalizedMatcher(AnyVararg.ANY_VARARG),
            new LocalizedMatcher(captor1.capture()),
            new LocalizedMatcher(captor2.capture())
        );
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, matchers);
        //when & then: should not throw exception
        invocationMatcher.captureArgumentsFrom(invocation);
    }
