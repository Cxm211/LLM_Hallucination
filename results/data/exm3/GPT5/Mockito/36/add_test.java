// org/mockitousage/spies/SpyingOnInterfacesTest.java::shouldFailInRuntimeWhenCallingRealMethodOnInterface
@Test
    public void shouldFailInRuntimeWhenCallingRealMethodOnAbstractClass() throws Exception {
        abstract class AbstractThing { abstract String foo(); }
        AbstractThing thing = mock(AbstractThing.class);
        when(thing.foo()).thenAnswer(
            new Answer() {
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    return invocation.callRealMethod();
                }
            }
        );
        try {
            thing.foo();
            fail();
        } catch (MockitoException e) {}
    }