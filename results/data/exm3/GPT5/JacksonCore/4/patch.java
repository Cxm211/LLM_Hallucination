public char[] expandCurrentSegment()
    {
        final char[] curr = _currentSegment;
        final int len = curr.length;
        // Grow by 50% by default; once at or above the maximum threshold, grow more slowly by 25%
        final int newLen = (len >= MAX_SEGMENT_LEN)
                ? (len + (len >> 2))
                : Math.min(MAX_SEGMENT_LEN, len + (len >> 1));
        return (_currentSegment = java.util.Arrays.copyOf(curr, newLen));
    }