// org/mockitousage/basicapi/MocksSerializationTest.java
@Test
public void shouldHaveMultipleExtraInterfacesAndBeSerializable() throws Exception {
    //when
    IMethods mock = mock(IMethods.class, withSettings().serializable().extraInterfaces(List.class, Cloneable.class));

    //then
    assertTrue(mock instanceof java.io.Serializable);
    assertTrue(mock instanceof List);
    assertTrue(mock instanceof Cloneable);
    serializeAndBack(mock);
}