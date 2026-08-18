// org/mockito/internal/invocation/InvocationMatcherTest.java
@Test
public void should_not_capture_when_matcher_is_not_capturing() throws Exception {
    // given
    mock.simpleMethod(42);
    Invocation invocation = getLastInvocation();

    // when - matcher doesn't implement CapturesArguments
    InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, 
        (List) asList(new LocalizedMatcher(new Equals(42))));

    // then - should not throw exception
    invocationMatcher.captureArgumentsFrom(invocation);
}