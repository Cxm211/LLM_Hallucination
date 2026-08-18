// org/mockitousage/spies/SpyingOnInterfacesTest.java
@Test
    public void shouldFailWhenCallingRealMethodOnInterfaceWithMultipleArguments() throws Exception {
        //given
        List list = mock(List.class);
        when(list.subList(0, 1)).thenAnswer(
            new Answer() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.callRealMethod();
                }
            }
        );
        try {
            //when
            list.subList(0, 1);
            //then
            fail();
        } catch (MockitoException e) {}
    }
