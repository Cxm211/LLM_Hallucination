int nextIndexOf(CharSequence seq) {
        char startChar = seq.charAt(0);
        int max = length - seq.length();
        for (int offset = pos; offset <= max; offset++) {
            if (startChar != input[offset])
                while (++offset <= max && startChar != input[offset]);
            if (offset <= max) {
                int i = offset + 1;
                int lastExclusive = offset + seq.length();
                for (int j = 1; i < lastExclusive && seq.charAt(j) == input[i]; i++, j++);
                if (i == lastExclusive)
                    return offset - pos;
            }
        }
        return -1;
    }