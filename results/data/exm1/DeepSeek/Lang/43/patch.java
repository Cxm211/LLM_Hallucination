private StringBuffer appendQuotedString(String pattern, ParsePosition pos, StringBuffer appendTo, boolean escapingOn) {
    int start = pos.getIndex();
    char[] c = pattern.toCharArray();
    if (escapingOn && pattern.startsWith(ESCAPED_QUOTE, start)) {
        if (appendTo != null) {
            appendTo.append(QUOTE);
        }
        pos.setIndex(start + ESCAPED_QUOTE.length());
        return appendTo;
    }
    int lastHold = start + 1;
    for (int i = start + 1; i < pattern.length(); i++) {
        if (escapingOn && pattern.startsWith(ESCAPED_QUOTE, i)) {
            if (appendTo != null) {
                appendTo.append(c, lastHold, i - lastHold);
                appendTo.append(QUOTE);
            }
            pos.setIndex(i + ESCAPED_QUOTE.length());
            lastHold = pos.getIndex();
            i = lastHold - 1;
            continue;
        }
        if (c[i] == QUOTE) {
            if (appendTo != null) {
                appendTo.append(c, lastHold, i - lastHold);
            }
            pos.setIndex(i + 1);
            return appendTo;
        }
    }
    throw new IllegalArgumentException("Unterminated quoted string at position " + start);
}