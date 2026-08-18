// org/mockitousage/bugs/ListenersLostOnResetMockTest.java
@Test
    public void shouldPreserveSerializableAfterReset() {
        List mock = mock(List.class, withSettings().serializable());
        reset(mock);
        assertTrue(mock instanceof Serializable);
    }
