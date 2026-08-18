// buggy function
    public void validate(Answer<?> answer, Invocation invocation) {
        if (answer instanceof ThrowsException) {
            validateException((ThrowsException) answer, invocation);
        }
        
        if (answer instanceof Returns) {
            validateReturnValue((Returns) answer, invocation);
        }
        
        if (answer instanceof DoesNothing) {
            validateDoNothing((DoesNothing) answer, invocation);
        }
        
    }

// trigger testcase
// org/mockito/internal/stubbing/answers/AnswersValidatorTest.java::shouldFailWhenCallingRealMethodOnIterface
@Test
    public void shouldFailWhenCallingRealMethodOnIterface() throws Throwable {
        //given
        Invocation inovcationOnIterface = new InvocationBuilder().method("simpleMethod").toInvocation();
        try {
            //when
            validator.validate(new CallsRealMethods(), inovcationOnIterface);
            //then
            fail();
        } catch (MockitoException e) {}
    }

// org/mockitousage/spies/SpyingOnInterfacesTest.java::shouldFailFastWhenCallingRealMethodOnInterface
@Test
    public void shouldFailFastWhenCallingRealMethodOnInterface() throws Exception {
        List list = mock(List.class);
        try {
            //when
            when(list.get(0)).thenCallRealMethod();
            //then
            fail();
        } catch (MockitoException e) {}
    }
