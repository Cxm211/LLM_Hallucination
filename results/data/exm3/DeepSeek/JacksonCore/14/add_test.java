// com/fasterxml/jackson/core/io/TestIOContext.java
public void testReleaseSameSizeBuffer() throws Exception
{
    IOContext ctxt = new IOContext(new BufferRecycler(), "N/A", true);
    // Test byte buffer
    byte[] originalByteBuf = ctxt.allocReadIOBuffer();
    byte[] sameSizeByteBuf = new byte[originalByteBuf.length];
    // Should not throw an exception after fix
    ctxt.releaseReadIOBuffer(sameSizeByteBuf);
    // Test char buffer
    char[] originalCharBuf = ctxt.allocTokenBuffer();
    char[] sameSizeCharBuf = new char[originalCharBuf.length];
    ctxt.releaseTokenBuffer(sameSizeCharBuf);
}
