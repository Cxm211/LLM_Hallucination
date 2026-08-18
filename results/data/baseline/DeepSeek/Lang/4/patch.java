    public LookupTranslator(final CharSequence[]... lookup) {
        lookupMap = new HashMap<CharSequence, CharSequence>();
        int _shortest = Integer.MAX_VALUE;
        int _longest = 0;
        if (lookup != null) {
            for (final CharSequence[] seq : lookup) {
                if (seq != null && seq.length >= 2) {
                    this.lookupMap.put(seq[0], seq[1]);
                    final int sz = seq[0].length();
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