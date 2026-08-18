// com/fasterxml/jackson/databind/ser/TestMapSerialization.java
public void testClassKeyInteger() throws IOException
    {
        Map<Class<?>,Integer> map = new LinkedHashMap<Class<?>,Integer>();
        map.put(Integer.class, 2);
        String json = MAPPER.writeValueAsString(map);
        assertEquals(aposToQuotes("{'java.lang.Integer':2}"), json);
    }
