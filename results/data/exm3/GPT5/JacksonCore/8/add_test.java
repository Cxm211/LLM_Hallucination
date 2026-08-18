// com/fasterxml/jackson/core/util/TestTextBuffer.java::testEmpty
public void testEmptyInitial() {
        TextBuffer tb = new TextBuffer(new BufferRecycler());
        // Without any reset, should be logically empty
        assertTrue(tb.getTextBuffer().length == 0);
        tb.contentsAsString();
        assertTrue(tb.getTextBuffer().length == 0);
    }