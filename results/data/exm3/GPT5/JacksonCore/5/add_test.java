// com/fasterxml/jackson/core/TestJsonPointer.java::testWonkyNumber173
public void testWonkyNumberOddIndex() throws Exception {
        JsonPointer ptr = JsonPointer.compile("/3x5");
        assertFalse(ptr.matches());
    }