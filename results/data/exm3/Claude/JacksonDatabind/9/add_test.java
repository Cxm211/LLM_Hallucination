// com/fasterxml/jackson/databind/ser/TestMapSerialization.java
public void testMultipleClassKeys() throws IOException
{
    Map<Class<?>,Integer> map = new LinkedHashMap<Class<?>,Integer>();
    map.put(Integer.class, 1);
    map.put(Double.class, 2);
    map.put(Boolean.class, 3);
    String json = MAPPER.writeValueAsString(map);
    assertEquals(aposToQuotes("{'java.lang.Integer':1,'java.lang.Double':2,'java.lang.Boolean':3}"), json);
}