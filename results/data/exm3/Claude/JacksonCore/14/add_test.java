// com/fasterxml/jackson/core/io/TestIOContext.java
public void testReleaseWithEqualSizedBuffer() throws Exception
{
    IOContext ctxt = new IOContext(new BufferRecycler(), "N/A", true);

    // Allocate a buffer
    byte[] readBuf = ctxt.allocReadIOBuffer();
    int originalSize = readBuf.length;

    // Create a different buffer with the same size
    byte[] sameSizeBuf = new byte[originalSize];

    // This should succeed (not throw) because sizes are equal
    ctxt.releaseReadIOBuffer(sameSizeBuf);

    // Test with char buffer
    char[] tokenBuf = ctxt.allocTokenBuffer();
    int originalCharSize = tokenBuf.length;

    char[] sameSizeCharBuf = new char[originalCharSize];

    // This should also succeed
    ctxt.releaseTokenBuffer(sameSizeCharBuf);
}