// org/mockitousage/spies/SpyingOnInterfacesTest.java
@Test
    public void shouldFailFastWhenCallingRealMethodOnAbstractClass() throws Exception {
        AbstractList list = mock(AbstractList.class);
        try {
            when(list.get(0)).thenCallRealMethod();
            fail();
        } catch (MockitoException e) {}
    }
