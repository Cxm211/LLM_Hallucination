// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testGetTextBufferMultipleCalls() {
    TextBuffer tb = new TextBuffer(new BufferRecycler());
    tb.resetWithEmpty();
    tb.contentsAsString();
    
    char[] buf1 = tb.getTextBuffer();
    char[] buf2 = tb.getTextBuffer();
    assertTrue(buf1.length == 0);
    assertTrue(buf2.length == 0);
    assertSame(buf1, buf2);
}