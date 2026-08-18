// org/mockito/internal/invocation/InvocationMatcherTest.java
public void should_capture_arguments_for_varargs_with_multiple_elements() throws Exception {
        //given
        mock.varargs("a", "b");
        Invocation invocation = getLastInvocation();

        //when
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new LocalizedMatcher(AnyVararg.ANY_VARARG)));

        //then
        invocationMatcher.captureArgumentsFrom(invocation);
    }
