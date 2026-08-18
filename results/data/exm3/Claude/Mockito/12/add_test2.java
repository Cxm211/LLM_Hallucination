// org/mockito/internal/util/reflection/GenericMasterTest.java
@Test
public void shouldHandleSimpleGenericType() throws Exception {
    Field stringListField = GenericMasterTestFixture.class.getDeclaredField("stringList");
    assertEquals(String.class, m.getGenericType(stringListField));
}