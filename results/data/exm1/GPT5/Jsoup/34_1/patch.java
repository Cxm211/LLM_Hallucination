    int nextIndexOf(CharSequence seq) {
        // handle sequences that may include surrogate pairs and avoid out-of-bounds
        char startChar = seq.charAt(0);
        int seqLen = seq.length();
        for (int offset = pos; offset < length; offset++) {
            // scan to first instance of startChar
            if (startChar != input[offset])
                while (++offset < length && startChar != input[offset]);

            if (offset >= length)
                break;

            // if not enough characters remain to match the sequence, no match possible
            if (offset + seqLen > length)
                return -1;

            int i = offset + 1;
            int last = offset + seqLen; // exclusive end index
            for (int j = 1; i < last && i < length && seq.charAt(j) == input[i]; i++, j++);
            if (i == last) // found full sequence
                return offset - pos;
        }
        return -1;
    }