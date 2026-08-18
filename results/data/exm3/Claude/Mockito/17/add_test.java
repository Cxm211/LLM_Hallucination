// org/mockitousage/basicapi/MocksSerializationTest.java
@Test
public void shouldBeSerializableWhenOnlySerializableSet() throws Exception {
    //when
    IMethods mock = mock(IMethods.class, withSettings().serializable());

    //then
    serializeAndBack(mock);
}