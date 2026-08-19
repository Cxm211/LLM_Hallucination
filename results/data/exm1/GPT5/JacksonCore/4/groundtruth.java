// ===== FIXED com.fasterxml.jackson.core.util.TextBuffer :: expandCurrentSegment() [lines 580-591] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonCore/JacksonCore-4-fixed/src/main/java/com/fasterxml/jackson/core/util/TextBuffer.java =====
    public char[] expandCurrentSegment()
    {
        final char[] curr = _currentSegment;
        // Let's grow by 50% by default
        final int len = curr.length;
        int newLen = len + (len >> 1);
        // but above intended maximum, slow to increase by 25%
        if (newLen > MAX_SEGMENT_LEN) {
            newLen = len + (len >> 2);
        }
        return (_currentSegment = Arrays.copyOf(curr, newLen));
    }
