boolean containsIgnoreCase(String seq) {
    // true case-insensitive search
    String lowerSeq = seq.toLowerCase(Locale.ENGLISH);
    int len = seq.length();
    if (len > bufLength - bufPos) {
        return false;
    }
    for (int i = 0; i <= bufLength - bufPos - len; i++) {
        boolean found = true;
        for (int j = 0; j < len; j++) {
            char c = charBuf[bufPos + i + j];
            if (Character.toLowerCase(c) != lowerSeq.charAt(j)) {
                found = false;
                break;
            }
        }
        if (found) {
            return true;
        }
    }
    return false;
}