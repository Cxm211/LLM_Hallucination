boolean containsIgnoreCase(String seq) {
    // used to check presence of </title>, </style>. only finds consistent case.
    int scanLength = seq.length();
    if (scanLength > bufLength) return false;
    int start = bufPos;
    int end = bufPos + bufLength - scanLength;
    for (int i = start; i <= end; i++) {
        boolean match = true;
        for (int j = 0; j < scanLength; j++) {
            char c1 = charBuf[i + j];
            char c2 = seq.charAt(j);
            if (Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                match = false;
                break;
            }
        }
        if (match) return true;
    }
    return false;
}