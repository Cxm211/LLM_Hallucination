// com/fasterxml/jackson/core/io/TestIOContext.java::testAllocations
public void testEqualSizedRelease() throws Exception {
        IOContext ctxt = new IOContext(new BufferRecycler(), "N/A", true);
        byte[] orig = ctxt.allocReadIOBuffer();
        byte[] sameSize = new byte[orig.length];
        // Should not throw: releasing different buffer of equal size
        ctxt.releaseReadIOBuffer(sameSize);
    }