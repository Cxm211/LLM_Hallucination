    public char[] expandCurrentSegment()
    {
        final char[] curr = _currentSegment;
        final int len = curr.length;
        int newLen;
        if (len < MAX_SEGMENT_LEN) {
            newLen = Math.min(MAX_SEGMENT_LEN, len + (len >> 1));
        } else {
            newLen = len + (len >> 2);
        }
        return (_currentSegment = Arrays.copyOf(curr, newLen));
    }