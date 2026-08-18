// org/mockitousage/bugs/ListenersLostOnResetMockTest.java
@Test
    public void shouldPreserveExtraInterfacesAfterReset() {
        List mock = mock(List.class, withSettings().extraInterfaces(Serializable.class));
        reset(mock);
        assertTrue(mock instanceof Serializable);
    }
