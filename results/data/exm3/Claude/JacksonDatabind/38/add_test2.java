// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java
public void testEmptyCollectionType() throws Exception
{
    JavaType elem = SimpleType.construct(Point.class);
    JavaType t = CollectionType.construct(List.class, elem);

    final String json = "[]";

    List<Point> l = MAPPER.readValue(json, t);
    assertNotNull(l);
    assertEquals(0, l.size());
}