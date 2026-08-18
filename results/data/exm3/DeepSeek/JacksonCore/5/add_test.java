// com/fasterxml/jackson/core/TestJsonPointer.java
public void testNonDigitOddIndexLength2() throws Exception
    {
        JsonPointer ptr = JsonPointer.compile("/0a");
        assertFalse(ptr.matches());
    }
