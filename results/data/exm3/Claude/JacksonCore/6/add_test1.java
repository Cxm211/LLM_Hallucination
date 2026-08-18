// com/fasterxml/jackson/core/TestJsonPointer.java
public void testValidIndicesWithoutLeadingZeroes() throws Exception
{
    JsonPointer ptr = JsonPointer.compile("/1");
    assertEquals(1, ptr.getMatchingIndex());
    ptr = JsonPointer.compile("/10");
    assertEquals(10, ptr.getMatchingIndex());
    ptr = JsonPointer.compile("/100");
    assertEquals(100, ptr.getMatchingIndex());
}