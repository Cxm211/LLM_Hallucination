// com/fasterxml/jackson/core/TestJsonPointer.java
public void testLeadingZeroThreeDigit() throws Exception {
    JsonPointer ptr = JsonPointer.compile("/001");
    assertEquals(-1, ptr.getMatchingIndex());
}
