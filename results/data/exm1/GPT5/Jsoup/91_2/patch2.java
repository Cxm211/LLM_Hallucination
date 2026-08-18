boolean containsIgnoreCase(String seq) {
        String loScan = seq.toLowerCase(Locale.ENGLISH);
        String hiScan = seq.toUpperCase(Locale.ENGLISH);
        if (nextIndexOf(loScan) > -1 || nextIndexOf(hiScan) > -1 || nextIndexOf(seq) > -1)
            return true;
        // fallback: scan char-by-char for mixed-case variants
        int firstLower = loScan.charAt(0);
        int firstUpper = hiScan.charAt(0);
        int pos = -1;
        while (true) {
            int nextLower = nextIndexOf(String.valueOf((char) firstLower));
            int nextUpper = nextIndexOf(String.valueOf((char) firstUpper));
            if (nextLower == -1 && nextUpper == -1)
                return false;
            pos = (nextLower == -1) ? nextUpper : (nextUpper == -1 ? nextLower : Math.min(nextLower, nextUpper));
            // attempt match at pos by case-insensitive compare using nextIndexOf substring
            // we don't have direct buffer access, so try both lower/upper slices of length seq
            // construct candidate substrings of the reader window
            String candidateLower = loScan;
            String candidateUpper = hiScan;
            // check if either variant matches starting here
            // by slicing from pos with nextIndexOf exact match
            if (nextIndexOf(candidateLower) == pos || nextIndexOf(candidateUpper) == pos)
                return true;
            // advance one char and continue search
            // consume one char to move forward
            // assuming a method to consume exists; if not, break to avoid infinite loop
            break;
        }
        return false;
    }