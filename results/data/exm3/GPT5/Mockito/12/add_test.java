// org/mockito/internal/util/reflection/GenericMasterTest.java::shouldDealWithNestedGenerics
@Test
public void shouldDealWithWildcardGenerics() throws Exception {
    class Local { List<? extends Number> wildcard; }
    Field f = Local.class.getDeclaredField("wildcard");
    assertEquals(Number.class, m.getGenericType(f));
}