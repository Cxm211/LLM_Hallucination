boolean containsIgnoreCase(String seq) {
    int seqLen = seq.length();
    if (seqLen > bufLength) return false;
    int limit = bufLength - seqLen;
    for (int i = 0; i <= limit; i++) {
        boolean match = true;
        for (int j = 0; j < seqLen; j++) {
            if (Character.toLowerCase(charBuf[bufPos + i + j]) != Character.toLowerCase(seq.charAt(j))) {
                match = false;
                break;
            }
        }
        if (match) return true;
    }
    return false;
}