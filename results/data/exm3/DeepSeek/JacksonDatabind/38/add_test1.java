// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java
public void testExplicitHashtableMapType() throws Exception {
    JavaType key = SimpleType.construct(String.class);
    JavaType elem = SimpleType.construct(Point.class);
    JavaType t = MapType.construct(Hashtable.class, key, elem);

    final String json = aposToQuotes("{'x':{'x':3,'y':5}}");        

    Hashtable<String,Point> m = MAPPER.readValue(json, t);
    assertNotNull(m);
    assertEquals(1, m.size());
    Object ob = m.values().iterator().next();
    assertEquals(Point.class, ob.getClass());
    Point p = (Point) ob;
    assertEquals(3, p.x);
    assertEquals(5, p.getY());
}
