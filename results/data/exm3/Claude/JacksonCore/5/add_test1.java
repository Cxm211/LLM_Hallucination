// com/fasterxml/jackson/core/TestJsonPointer.java
public void testWonkyNumberOverflow() throws Exception
    {
        JsonPointer ptr = JsonPointer.compile("/2147483648");
        assertFalse(ptr.matches());
    }