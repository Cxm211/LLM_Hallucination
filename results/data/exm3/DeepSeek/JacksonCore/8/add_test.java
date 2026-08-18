// com/fasterxml/jackson/core/util/TestTextBuffer.java
public void testGetTextBufferAfterReleaseBuffers() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        tb.append('a');
        tb.releaseBuffers();
        assertTrue(tb.getTextBuffer().length == 0);
    }
