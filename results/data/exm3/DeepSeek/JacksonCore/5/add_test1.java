// com/fasterxml/jackson/core/TestJsonPointer.java
public void testNonDigitOddIndexLength10() throws Exception
    {
        JsonPointer ptr = JsonPointer.compile("/123456789a");
        assertFalse(ptr.matches());
    }
