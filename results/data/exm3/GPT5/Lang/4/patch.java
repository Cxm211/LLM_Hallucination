public LookupTranslator(final CharSequence[]... lookup) {
        lookupMap = new HashMap<CharSequence, CharSequence>();
        int _shortest = Integer.MAX_VALUE;
        int _longest = 0;
        if (lookup != null) {
            for (final CharSequence[] seq : lookup) {
                if (seq == null || seq.length < 2 || seq[0] == null || seq[1] == null) {
                    continue;
                }
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
        shortest = _shortest;
        longest = _longest;
    }