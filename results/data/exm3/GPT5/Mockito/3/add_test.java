// org/mockito/internal/invocation/InvocationMatcherTest.java::should_not_capture_when_no_varargs
@Test
public void should_not_capture_when_no_varargs() throws Exception {
    // given
    mock.mixedVarargs(1);
    Invocation invocation = getLastInvocation();
    CapturingMatcher m = new CapturingMatcher();
    InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new Equals(1), new LocalizedMatcher(m)));

    // when
    invocationMatcher.captureArgumentsFrom(invocation);

    // then
    Assertions.assertThat(m.getAllValues()).isEmpty();
}