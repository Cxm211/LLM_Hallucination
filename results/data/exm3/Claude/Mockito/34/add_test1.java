// org/mockito/internal/invocation/InvocationMatcherTest.java
@Test
public void shouldHandleCaptureWithMultipleMatchersAndFewerArguments() throws Exception {
    //given
    mock.varargs(1, 2);
    Invocation invocation = getLastInvocation();

    //when
    InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(
        new LocalizedMatcher(ArgumentMatchers.any()),
        new LocalizedMatcher(ArgumentMatchers.any()),
        new LocalizedMatcher(ArgumentMatchers.any()),
        new LocalizedMatcher(ArgumentMatchers.any())
    ));

    //then
    invocationMatcher.captureArgumentsFrom(invocation);
}