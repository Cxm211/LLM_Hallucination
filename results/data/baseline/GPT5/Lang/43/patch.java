private StringBuffer appendQuotedString(String pattern, ParsePosition pos,
            StringBuffer appendTo, boolean escapingOn) {
        int start = pos.getIndex();
        char[] c = pattern.toCharArray();
        if (start >= pattern.length()) {
            throw new IllegalArgumentException(
                    "Unterminated quoted string at position " + start);
        }
        if (escapingOn && c[start] == QUOTE) {
            next(pos);
            return appendTo == null ? null : appendTo.append(QUOTE);
        }
        int lastHold = start;
        while (pos.getIndex() < pattern.length()) {
            int i = pos.getIndex();
            if (escapingOn && pattern.startsWith(ESCAPED_QUOTE, i)) {
                appendTo.append(c, lastHold, i - lastHold).append(QUOTE);
                pos.setIndex(i + ESCAPED_QUOTE.length());
                lastHold = pos.getIndex();
                continue;
            }
            switch (c[i]) {
            case QUOTE:
                next(pos);
                return appendTo == null ? null : appendTo.append(c, lastHold,
                        pos.getIndex() - lastHold);
            default:
                next(pos);
            }
        }
        throw new IllegalArgumentException(
                "Unterminated quoted string at position " + start);
    }