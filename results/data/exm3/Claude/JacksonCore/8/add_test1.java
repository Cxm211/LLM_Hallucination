// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testGetTextBufferWithResultString() {
    TextBuffer tb = new TextBuffer(new BufferRecycler());
    tb.append("hello");
    String str = tb.contentsAsString();
    char[] buf = tb.getTextBuffer();
    assertTrue(buf.length == 5);
    assertEquals("hello", new String(buf, 0, 5));
}