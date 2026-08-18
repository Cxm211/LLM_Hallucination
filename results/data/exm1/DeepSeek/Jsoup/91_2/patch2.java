boolean containsIgnoreCase(String seq) {
        // case-insensitive scanning
        int seqLen = seq.length();
        if (seqLen == 0) return true;
        int limit = bufLength - seqLen;
        for (int i = bufPos; i <= limit; i++) {
            boolean match = true;
            for (int j = 0; j < seqLen; j++) {
                if (Character.toLowerCase(charBuf[i+j]) != Character.toLowerCase(seq.charAt(j))) {
                    match = false;
                    break;
                }
            }
            if (match) return true;
        }
        return false;
    }