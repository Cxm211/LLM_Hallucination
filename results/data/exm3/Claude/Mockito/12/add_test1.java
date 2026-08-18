// org/mockito/internal/util/reflection/GenericMasterTest.java
@Test
public void shouldHandleRawType() throws Exception {
    Field rawField = GenericMasterTestFixture.class.getDeclaredField("rawField");
    assertEquals(Object.class, m.getGenericType(rawField));
}