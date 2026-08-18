// com/fasterxml/jackson/databind/ser/TestMapSerialization.java
public void testArrayClassKey() throws IOException
{
    Map<Class<?>,String> map = new LinkedHashMap<Class<?>,String>();
    map.put(String[].class, "array");
    String json = MAPPER.writeValueAsString(map);
    assertEquals(aposToQuotes("{'[Ljava.lang.String;':'array'}"), json);
}