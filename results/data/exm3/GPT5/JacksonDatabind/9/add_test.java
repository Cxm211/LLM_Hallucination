// com/fasterxml/jackson/databind/ser/TestMapSerialization.java::testClassKeyArray
public void testClassKeyArray() throws IOException
    {
        Map<Class<?>,Integer> map = new LinkedHashMap<Class<?>,Integer>();
        map.put(String[].class, 1);
        String json = MAPPER.writeValueAsString(map);
        assertEquals(aposToQuotes("{'[Ljava.lang.String;':1}"), json);
    }