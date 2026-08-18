// org/mockitousage/basicapi/MocksSerializationTest.java
@Test
public void shouldNotBeSerializableWhenOnlyExtraInterfacesSet() throws Exception {
    //when
    IMethods mock = mock(IMethods.class, withSettings().extraInterfaces(List.class));

    //then
    assertFalse(mock instanceof java.io.Serializable);
}