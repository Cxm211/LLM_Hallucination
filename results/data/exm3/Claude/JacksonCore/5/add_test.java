// com/fasterxml/jackson/core/TestJsonPointer.java
public void testWonkyNumberAtStart() throws Exception
    {
        JsonPointer ptr = JsonPointer.compile("/a123");
        assertFalse(ptr.matches());
    }