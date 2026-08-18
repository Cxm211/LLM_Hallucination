// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testGetTextBufferAfterAppend() {
    TextBuffer tb = new TextBuffer(new BufferRecycler());
    tb.append("test");
    char[] buf1 = tb.getTextBuffer();
    assertTrue(buf1.length >= 4);
    
    tb.resetWithEmpty();
    char[] buf2 = tb.getTextBuffer();
    assertTrue(buf2.length == 0);
}