// org/mockito/internal/util/reflection/GenericMasterTest.java
@Test
public void shouldHandleNonParameterizedGenericType() throws Exception {
    Field simpleField = GenericMasterTestFixture.class.getDeclaredField("simpleList");
    assertEquals(Object.class, m.getGenericType(simpleField));
}