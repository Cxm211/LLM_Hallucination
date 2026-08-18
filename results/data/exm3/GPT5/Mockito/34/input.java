// buggy function
    public void captureArgumentsFrom(Invocation i) {
        int k = 0;
        for (Matcher m : matchers) {
            if (m instanceof CapturesArguments) {
                ((CapturesArguments) m).captureFrom(i.getArguments()[k]);
            }
            k++;
        }
    }

// trigger testcase
// org/mockito/internal/invocation/InvocationMatcherTest.java::shouldMatchCaptureArgumentsWhenArgsCountDoesNOTMatch
@Test
    public void shouldMatchCaptureArgumentsWhenArgsCountDoesNOTMatch() throws Exception {
        //given
        mock.varargs();
        Invocation invocation = getLastInvocation();

        //when
        InvocationMatcher invocationMatcher = new InvocationMatcher(invocation, (List) asList(new LocalizedMatcher(AnyVararg.ANY_VARARG)));

        //then
        invocationMatcher.captureArgumentsFrom(invocation);
    }

// org/mockitousage/basicapi/UsingVarargsTest.java::shouldMatchEasilyEmptyVararg
public void shouldMatchEasilyEmptyVararg() throws Exception {
        //when
        when(mock.foo(anyVararg())).thenReturn(-1);

        //then
        assertEquals(-1, mock.foo());
    }
