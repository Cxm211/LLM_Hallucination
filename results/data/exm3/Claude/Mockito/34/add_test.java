// org/mockito/internal/invocation/InvocationMatcherTest.java
@Test
public void shouldHandleCaptureWhenMoreMatchersThanArguments() throws Exception {
    //given
    mock.simpleMethod(1);
    Invocation invocation = getLastInvocation();

    //when
    InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(
        new LocalizedMatcher(ArgumentMatchers.any()),
        new LocalizedMatcher(ArgumentMatchers.any())
    ));

    //then
    invocationMatcher.captureArgumentsFrom(invocation);
}