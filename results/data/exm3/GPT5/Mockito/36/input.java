// buggy function
    public Object callRealMethod() throws Throwable {
        return realMethod.invoke(mock, rawArguments);
    }

// trigger testcase
// org/mockito/internal/invocation/InvocationTest.java::shouldScreamWhenCallingRealMethodOnInterface
@Test
    public void shouldScreamWhenCallingRealMethodOnInterface() throws Throwable {
        //given
        Invocation invocationOnInterface = new InvocationBuilder().toInvocation();

        try {
            //when
            invocationOnInterface.callRealMethod();
            //then
            fail();
        } catch(MockitoException e) {}
    }

// org/mockitousage/spies/SpyingOnInterfacesTest.java::shouldFailInRuntimeWhenCallingRealMethodOnInterface
@Test
    public void shouldFailInRuntimeWhenCallingRealMethodOnInterface() throws Exception {
        //given
        List list = mock(List.class);
        when(list.get(0)).thenAnswer(
            new Answer() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.callRealMethod();
                }
            }
        );
        try {
            //when
            list.get(0);            
            //then
            fail();
        } catch (MockitoException e) {}
    }
