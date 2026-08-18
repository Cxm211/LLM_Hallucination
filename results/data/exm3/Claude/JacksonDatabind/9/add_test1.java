// com/fasterxml/jackson/databind/ser/TestMapSerialization.java
public void testPrimitiveClassKeys() throws IOException
{
    Map<Class<?>,String> map = new LinkedHashMap<Class<?>,String>();
    map.put(int.class, "int");
    map.put(long.class, "long");
    String json = MAPPER.writeValueAsString(map);
    assertEquals(aposToQuotes("{'int':'int','long':'long'}"), json);
}