// org/mockito/internal/invocation/InvocationMatcherTest.java::shouldMatchCaptureArgumentsWhenArgsCountDoesNOTMatch
@Test
public void shouldSkipExtraMatchersWhenMoreMatchersThanArgs() throws Exception {
    // given
    mock.varargs("one");
    Invocation invocation = getLastInvocation();

    // when
    InvocationMatcher invocationMatcher = new InvocationMatcher(
            invocation,
            (List) asList(new LocalizedMatcher(AnyVararg.ANY_VARARG), new LocalizedMatcher(AnyVararg.ANY_VARARG))
    );

    // then
    invocationMatcher.captureArgumentsFrom(invocation);
}