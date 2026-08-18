public char[] expandCurrentSegment()
    {
        final char[] curr = _currentSegment;
        final int len = curr.length;
        int newLen;
        if (len >= MAX_SEGMENT_LEN) {
            newLen = len + (len >> 2);
        } else {
            newLen = Math.min(MAX_SEGMENT_LEN, len + (len >> 1));
        }
        return (_currentSegment = Arrays.copyOf(curr, newLen));
    }