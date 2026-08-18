// org/mockito/internal/invocation/InvocationMatcherTest.java::should_capture_arguments_when_args_count_does_NOT_match
public void should_capture_arguments_when_args_count_does_NOT_match_with_non_empty_varargs() throws Exception {
        //given
        mock.varargs("a", "b");
        Invocation invocation = getLastInvocation();

        //when
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new LocalizedMatcher(AnyVararg.ANY_VARARG)));

        //then
        invocationMatcher.captureArgumentsFrom(invocation);
    }