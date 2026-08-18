// org/mockitousage/spies/SpyingOnInterfacesTest.java::shouldFailFastWhenCallingRealMethodOnAbstractMethod
@Test
public void shouldFailFastWhenCallingRealMethodOnAbstractMethod() throws Exception {
    abstract class AbstractFoo { abstract Object get(); }
    AbstractFoo mock = mock(AbstractFoo.class);
    try {
        when(mock.get()).thenCallRealMethod();
        fail();
    } catch (MockitoException e) {}
}