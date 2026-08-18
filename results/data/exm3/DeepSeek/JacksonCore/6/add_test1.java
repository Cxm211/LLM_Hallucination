// com/fasterxml/jackson/core/TestJsonPointer.java
public void testLeadingZeroTenDigits() throws Exception {
    JsonPointer ptr = JsonPointer.compile("/0000000000");
    assertEquals(-1, ptr.getMatchingIndex());
}
