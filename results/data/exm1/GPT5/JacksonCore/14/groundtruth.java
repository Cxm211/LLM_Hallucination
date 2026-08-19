// ===== FIXED com.fasterxml.jackson.core.io.IOContext :: _verifyRelease(byte[], byte[]) [lines 272-275] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-14-fixed/src/main/java/com/fasterxml/jackson/core/io/IOContext.java =====
    protected final void _verifyRelease(byte[] toRelease, byte[] src) {
        // 07-Mar-2016, tatu: As per [core#255], only prevent shrinking of buffer
        if ((toRelease != src) && (toRelease.length < src.length)) { throw wrongBuf(); }
    }

// ===== FIXED com.fasterxml.jackson.core.io.IOContext :: _verifyRelease(char[], char[]) [lines 277-280] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-14-fixed/src/main/java/com/fasterxml/jackson/core/io/IOContext.java =====
    protected final void _verifyRelease(char[] toRelease, char[] src) {
        // 07-Mar-2016, tatu: As per [core#255], only prevent shrinking of buffer
        if ((toRelease != src) && (toRelease.length < src.length)) { throw wrongBuf(); }
    }

// ===== FIXED com.fasterxml.jackson.core.io.IOContext :: wrongBuf() [lines 282-285] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-14-fixed/src/main/java/com/fasterxml/jackson/core/io/IOContext.java =====
    private IllegalArgumentException wrongBuf() {
        // sanity check failed; trying to return different, smaller buffer.
        return new IllegalArgumentException("Trying to release buffer smaller than original");
    }
