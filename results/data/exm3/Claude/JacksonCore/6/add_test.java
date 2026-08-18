// com/fasterxml/jackson/core/TestJsonPointer.java
public void testLeadingZeroesRejected() throws Exception
{
    JsonPointer ptr = JsonPointer.compile("/01");
    assertEquals(-1, ptr.getMatchingIndex());
    ptr = JsonPointer.compile("/001");
    assertEquals(-1, ptr.getMatchingIndex());
    ptr = JsonPointer.compile("/0123");
    assertEquals(-1, ptr.getMatchingIndex());
}