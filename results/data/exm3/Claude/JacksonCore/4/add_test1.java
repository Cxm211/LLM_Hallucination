// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testExpandBeyondMaxSegmentLen()
{
    TextBuffer tb = new TextBuffer(new BufferRecycler());
    char[] buf = tb.getCurrentSegment();
    
    // Expand until we exceed MAX_SEGMENT_LEN
    while (buf.length <= TextBuffer.MAX_SEGMENT_LEN) {
        buf = tb.expandCurrentSegment();
    }
    
    // Verify we're now at MAX_SEGMENT_LEN + 1
    assertEquals(TextBuffer.MAX_SEGMENT_LEN + 1, buf.length);
    
    // Further expansion should continue growing
    char[] oldBuf = buf;
    buf = tb.expandCurrentSegment();
    
    assertTrue("Buffer should continue to expand beyond MAX_SEGMENT_LEN + 1", buf.length > oldBuf.length);
}