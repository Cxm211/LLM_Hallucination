// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testExpandAboveMax() {
    TextBuffer tb = new TextBuffer(new BufferRecycler());
    char[] buf = tb.getCurrentSegment();
    while (buf.length <= TextBuffer.MAX_SEGMENT_LEN) {
        buf = tb.expandCurrentSegment();
    }
    int oldLen = buf.length;
    buf = tb.expandCurrentSegment();
    if (buf.length <= oldLen) {
        fail("Expected expansion from " + oldLen + " but got " + buf.length);
    }
}
