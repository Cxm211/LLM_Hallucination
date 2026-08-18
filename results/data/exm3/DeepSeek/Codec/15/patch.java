    private char getMappingCode(final String str, final int index) {
        // map() throws IllegalArgumentException
        final char mappedChar = this.map(str.charAt(index));
        // HW rule check
        if (index > 1 && mappedChar != '0') {
            final char hwChar = str.charAt(index - 1);
            if ('H' == hwChar || 'W' == hwChar) {
                // find previous non-H/W character
                int prev = index - 2;
                while (prev >= 0 && ('H' == str.charAt(prev) || 'W' == str.charAt(prev))) {
                    prev--;
                }
                if (prev >= 0) {
                    final char firstCode = this.map(str.charAt(prev));
                    if (firstCode == mappedChar) {
                        return 0;
                    }
                }
            }
        }
        return mappedChar;
    }