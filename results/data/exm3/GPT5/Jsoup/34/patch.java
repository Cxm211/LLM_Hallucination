int nextIndexOf(CharSequence seq) {
        int seqLen = seq.length();
        if (seqLen == 0) return 0;
        char startChar = seq.charAt(0);
        int max = length - seqLen; // last valid start index
        for (int offset = pos; offset <= max; offset++) {
            if (input[offset] != startChar)
                while (++offset <= max && input[offset] != startChar);
            if (offset > max)
                break;
            int i = offset + 1;
            int j = 1;
            int last = offset + seqLen; // exclusive end
            while (i < last && seq.charAt(j) == input[i]) { i++; j++; }
            if (i == last)
                return offset - pos;
        }
        return -1;
    }