// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java
public void testExplicitStackCollectionType() throws Exception {
    JavaType elem = SimpleType.construct(Point.class);
    JavaType t = CollectionType.construct(Stack.class, elem);

    final String json = aposToQuotes("[ {'x':1,'y':2}, {'x':3,'y':6 }]");        

    Stack<Point> l = MAPPER.readValue(json, t);
    assertNotNull(l);
    assertEquals(2, l.size());
    Object ob = l.get(0);
    assertEquals(Point.class, ob.getClass());
    Point p = (Point) ob;
    assertEquals(1, p.x);
    assertEquals(2, p.getY());
}
