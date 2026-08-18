// org/mockitousage/bugs/ListenersLostOnResetMockTest.java
@Test
public void serializableSettingPreservedAfterReset() throws Exception {
    List mockedList = mock(List.class, withSettings().serializable());
    reset(mockedList);

    assertTrue(mockedList instanceof java.io.Serializable);
}