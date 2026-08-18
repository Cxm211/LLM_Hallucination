// com/fasterxml/jackson/core/io/TestIOContext.java
public void testReleaseWithLargerBuffer() throws Exception
{
    IOContext ctxt = new IOContext(new BufferRecycler(), "N/A", true);

    // Allocate write encoding buffer
    byte[] writeBuf = ctxt.allocWriteEncodingBuffer();
    int originalSize = writeBuf.length;

    // Create a larger buffer
    byte[] largerBuf = new byte[originalSize * 2];

    // This should succeed (not throw) because larger buffers are allowed
    ctxt.releaseWriteEncodingBuffer(largerBuf);

    // Test with concat buffer
    char[] concatBuf = ctxt.allocConcatBuffer();
    int originalCharSize = concatBuf.length;

    char[] largerCharBuf = new char[originalCharSize + 100];

    // This should also succeed
    ctxt.releaseConcatBuffer(largerCharBuf);
}