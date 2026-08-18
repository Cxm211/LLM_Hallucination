private void printAndQuote(final Object object, final CharSequence value, final int offset, final int len,
            final Appendable out, final boolean newRecord) throws IOException {
        boolean quote = false;
        int start = offset;
        int pos = offset;
        final int end = offset + len;

        final char delimChar = getDelimiter();
        final Character quoteCharObj = getQuoteCharacter();
        final boolean hasQuoteChar = quoteCharObj != null;
        final char quoteChar = hasQuoteChar ? quoteCharObj.charValue() : 0;

        QuoteMode quoteModePolicy = getQuoteMode();
        if (quoteModePolicy == null) {
            quoteModePolicy = QuoteMode.MINIMAL;
        }

        // If no quote character is set, we cannot quote; fall back to escaping behavior.
        if (!hasQuoteChar) {
            printAndEscape(value, offset, len, out);
            return;
        }

        switch (quoteModePolicy) {
        case ALL:
            quote = true;
            break;
        case ALL_NON_NULL:
            quote = object != null; // only quote non-null objects
            break;
        case NON_NUMERIC:
            quote = !(object instanceof Number);
            break;
        case NONE:
            // Use the existing escaping code
            printAndEscape(value, offset, len, out);
            return;
        case MINIMAL:
            if (len <= 0) {
                // always quote an empty token that is the first on the line
                if (newRecord) {
                    quote = true;
                }
            } else {
                char c = value.charAt(pos);

                if (newRecord && (c < 0x20 || c > 0x21 && c < 0x23 || c > 0x2B && c < 0x2D || c > 0x7E)) {
                    quote = true;
                } else if (c <= COMMENT) {
                    quote = true;
                } else {
                    while (pos < end) {
                        c = value.charAt(pos);
                        if (c == LF || c == CR || c == quoteChar || c == delimChar) {
                            quote = true;
                            break;
                        }
                        pos++;
                    }

                    if (!quote) {
                        pos = end - 1;
                        c = value.charAt(pos);
                        if (c <= SP) {
                            quote = true;
                        }
                    }
                }
            }

            if (!quote) {
                out.append(value, start, end);
                return;
            }
            break;
        default:
            throw new IllegalStateException("Unexpected Quote value: " + quoteModePolicy);
        }

        if (!quote) {
            out.append(value, start, end);
            return;
        }

        // we hit something that needed encapsulation
        out.append(quoteChar);

        // Pick up where we left off: pos should be positioned on the first character that caused
        // the need for encapsulation.
        while (pos < end) {
            final char c = value.charAt(pos);
            if (c == quoteChar) {
                // write out the chunk up until this point
                // add 1 to the length to write out the encapsulator also
                out.append(value, start, pos + 1);
                // put the next starting position on the encapsulator so we will
                // write it out again with the next string (effectively doubling it)
                start = pos;
            }
            pos++;
        }

        // write the last segment
        out.append(value, start, pos);
        out.append(quoteChar);
    }