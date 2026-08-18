// org/mockito/internal/invocation/InvocationMatcherTest.java
public void should_capture_arguments_for_varargs_with_null_array() throws Exception {
        //given
        mock.varargs((String[])null);
        Invocation invocation = getLastInvocation();

        //when
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new LocalizedMatcher(AnyVararg.ANY_VARARG)));

        //then
        invocationMatcher.captureArgumentsFrom(invocation);
    }
