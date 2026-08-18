// ===== FIXED org.apache.commons.lang3.text.translate.LookupTranslator :: LookupTranslator [lines 45-63] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-4-fixed/src/main/java/org/apache/commons/lang3/text/translate/LookupTranslator.java =====
    public LookupTranslator(final CharSequence[]... lookup) {
        lookupMap = new HashMap<String, CharSequence>();
        int _shortest = Integer.MAX_VALUE;
        int _longest = 0;
        if (lookup != null) {
            for (final CharSequence[] seq : lookup) {
                this.lookupMap.put(seq[0].toString(), seq[1]);
                final int sz = seq[0].length();
                if (sz < _shortest) {
                    _shortest = sz;
                }
                if (sz > _longest) {
                    _longest = sz;
                }
            }
        }
        shortest = _shortest;
        longest = _longest;
    }

// ===== FIXED org.apache.commons.lang3.text.translate.LookupTranslator :: translate(CharSequence, int, Writer) [lines 69-84] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-4-fixed/src/main/java/org/apache/commons/lang3/text/translate/LookupTranslator.java =====
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        int max = longest;
        if (index + longest > input.length()) {
            max = input.length() - index;
        }
        // descend so as to get a greedy algorithm
        for (int i = max; i >= shortest; i--) {
            final CharSequence subSeq = input.subSequence(index, index + i);
            final CharSequence result = lookupMap.get(subSeq.toString());
            if (result != null) {
                out.write(result.toString());
                return i;
            }
        }
        return 0;
    }
