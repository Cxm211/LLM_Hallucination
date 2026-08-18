// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testExpandAtMaxSegmentLen()
{
    TextBuffer tb = new TextBuffer(new BufferRecycler());
    char[] buf = tb.getCurrentSegment();
    
    // Expand until we reach exactly MAX_SEGMENT_LEN
    while (buf.length < TextBuffer.MAX_SEGMENT_LEN) {
        buf = tb.expandCurrentSegment();
    }
    
    // At this point buf.length should equal MAX_SEGMENT_LEN
    assertEquals(TextBuffer.MAX_SEGMENT_LEN, buf.length);
    
    // Now expand one more time - should grow to MAX_SEGMENT_LEN + 1
    char[] oldBuf = buf;
    buf = tb.expandCurrentSegment();
    
    assertTrue("Buffer should have expanded beyond MAX_SEGMENT_LEN", buf.length > oldBuf.length);
    assertEquals("Buffer should be exactly MAX_SEGMENT_LEN + 1", TextBuffer.MAX_SEGMENT_LEN + 1, buf.length);
}