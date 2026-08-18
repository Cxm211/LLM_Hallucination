// com/fasterxml/jackson/databind/deser/ReadOrWriteOnlyTest.java
public void testWriteOnlyProperty() throws Exception
    {
        String json = MAPPER.writeValueAsString(new ReadXWriteY());
        assertEquals("{\"x\":1}", json);
        
        ReadXWriteY result = MAPPER.readValue("{\"y\":10}", ReadXWriteY.class);
        assertNotNull(result);
        assertEquals(1, result.x);
        assertEquals(10, result.y);
    }