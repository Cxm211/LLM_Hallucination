// ===== FIXED org.jsoup.parser.CharacterReader :: nextIndexOf(CharSequence) [lines 82-98] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Jsoup/Jsoup-34-fixed/src/main/java/org/jsoup/parser/CharacterReader.java =====
    int nextIndexOf(CharSequence seq) {
        // doesn't handle scanning for surrogates
        char startChar = seq.charAt(0);
        for (int offset = pos; offset < length; offset++) {
            // scan to first instance of startchar:
            if (startChar != input[offset])
                while(++offset < length && startChar != input[offset]);
            int i = offset + 1;
            int last = i + seq.length()-1;
            if (offset < length && last <= length) {
                for (int j = 1; i < last && seq.charAt(j) == input[i]; i++, j++);
                if (i == last) // found full sequence
                    return offset - pos;
            }
        }
        return -1;
    }
