    private StringBuffer appendQuotedString(String pattern, ParsePosition pos,
            StringBuffer appendTo, boolean escapingOn) {
        int start = pos.getIndex();
        char[] c = pattern.toCharArray();
        boolean nullAppend = appendTo == null;
        StringBuffer buffer = nullAppend ? new StringBuffer() : appendTo;

        // If escaping is on and we are at a quote, skip the opening quote.
        if (escapingOn && c[start] == QUOTE) {
            next(pos);
        }
        int lastHold = pos.getIndex();
        for (int i = pos.getIndex(); i < pattern.length(); i = pos.getIndex()) {
            if (escapingOn && pattern.substring(i).startsWith(ESCAPED_QUOTE)) {
                buffer.append(c, lastHold, i - lastHold).append(QUOTE);
                pos.setIndex(i + ESCAPED_QUOTE.length());
                lastHold = pos.getIndex();
                continue;
            }
            switch (c[i]) {
            case QUOTE:
                next(pos);
                buffer.append(c, lastHold, i - lastHold);
                return nullAppend ? null : buffer;
            default:
                next(pos);
            }
        }
        throw new IllegalArgumentException(
                "Unterminated quoted string at position " + start);
    }