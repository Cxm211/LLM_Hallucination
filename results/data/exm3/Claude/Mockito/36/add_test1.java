// org/mockitousage/spies/SpyingOnInterfacesTest.java
@Test
public void shouldFailWhenMethodDeclaringClassIsInterface() throws Exception {
    //given
    abstract class AbstractListImpl implements List {
        // Abstract class implementing interface
    }
    AbstractListImpl mockList = mock(AbstractListImpl.class);
    when(mockList.size()).thenAnswer(
        new Answer<Integer>() {
            public Integer answer(InvocationOnMock invocation) throws Throwable {
                return (Integer) invocation.callRealMethod();
            }
        }
    );
    try {
        //when
        mockList.size();
        //then
        fail();
    } catch (MockitoException e) {}
}