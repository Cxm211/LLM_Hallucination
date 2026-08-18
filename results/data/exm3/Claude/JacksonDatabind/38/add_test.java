// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java
public void testNestedCollectionType() throws Exception
{
    JavaType elem = SimpleType.construct(Point.class);
    JavaType innerCollection = CollectionType.construct(List.class, elem);
    JavaType outerCollection = CollectionType.construct(List.class, innerCollection);

    final String json = aposToQuotes("[ [ {'x':1,'y':2} ], [ {'x':3,'y':4}, {'x':5,'y':6} ] ]");

    List<List<Point>> result = MAPPER.readValue(json, outerCollection);
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(1, result.get(0).size());
    assertEquals(2, result.get(1).size());
    Point p = result.get(1).get(1);
    assertEquals(5, p.x);
    assertEquals(6, p.getY());
}