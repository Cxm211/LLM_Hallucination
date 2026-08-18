// org/mockito/internal/invocation/InvocationMatcherTest.java
@Test
public void should_capture_arguments_when_matchers_exceed_invocation_args() throws Exception {
    // given
    mock.simpleMethod(100);
    Invocation invocation = getLastInvocation();

    // when - more matchers than actual arguments
    InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, 
        (List) asList(new LocalizedMatcher(new Equals(100)), 
                     new LocalizedMatcher(new Equals(200))));

    // then - should not throw exception
    invocationMatcher.captureArgumentsFrom(invocation);
}