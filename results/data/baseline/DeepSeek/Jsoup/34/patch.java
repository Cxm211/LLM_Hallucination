int nextIndexOf(CharSequence seq) {
    char startChar = seq.charAt(0);
    for (int offset = pos; offset + seq.length() <= length; offset++) {
        if (startChar != input[offset]) {
            while (++offset < length && startChar != input[offset]);
            if (offset >= length) break;
            if (offset + seq.length() > length) break;
        }
        boolean match = true;
        for (int i = offset + 1, j = 1; i < offset + seq.length(); i++, j++) {
            if (input[i] != seq.charAt(j)) {
                match = false;
                break;
            }
        }
        if (match) return offset - pos;
    }
    return -1;
}