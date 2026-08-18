    public LookupTranslator(final CharSequence[]... lookup) {
        lookupMap = new HashMap<CharSequence, CharSequence>();
        int _shortest = Integer.MAX_VALUE;
        int _longest = 0;
        if (lookup != null) {
            for (final CharSequence[] seq : lookup) {
                if (seq != null && seq.length >= 2 && seq[0] != null && seq[1] != null) {
                    final String key = seq[0].toString();
                    final String value = seq[1].toString();
                    this.lookupMap.put(key, value);
                    final int sz = key.length();
                    if (sz < _shortest) {
                        _shortest = sz;
                    }
                    if (sz > _longest) {
                        _longest = sz;
                    }
                }
            }
        }
        shortest = _shortest;
        longest = _longest;
    }

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