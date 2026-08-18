// com/fasterxml/jackson/databind/interop/DeprecatedTypeHandling1102Test.java
public void testMapWithComplexValue() throws Exception
{
    JavaType key = SimpleType.construct(String.class);
    JavaType innerKey = SimpleType.construct(String.class);
    JavaType innerValue = SimpleType.construct(Integer.class);
    JavaType innerMap = MapType.construct(Map.class, innerKey, innerValue);
    JavaType outerMap = MapType.construct(Map.class, key, innerMap);

    final String json = aposToQuotes("{'outer':{'a':1,'b':2}}");

    Map<String, Map<String, Integer>> result = MAPPER.readValue(json, outerMap);
    assertNotNull(result);
    assertEquals(1, result.size());
    Map<String, Integer> inner = result.get("outer");
    assertNotNull(inner);
    assertEquals(2, inner.size());
    assertEquals(Integer.valueOf(1), inner.get("a"));
    assertEquals(Integer.valueOf(2), inner.get("b"));
}