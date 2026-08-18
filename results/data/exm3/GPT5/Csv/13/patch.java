private void print(final Object object, final CharSequence value, final int offset, final int len)
            throws IOException {
        if (!newRecord) {
            out.append(format.getDelimiter());
        }
        // Handle null values by writing the configured null string (or nothing if none)
        if (value == null) {
            final String nullString = format.getNullString();
            if (nullString != null) {
                out.append(nullString);
            }
            newRecord = false;
            return;
        }
        if (format.isQuoteCharacterSet()) {
            // the original object is needed so can check for Number
            printAndQuote(object, value, offset, len);
        } else if (format.isEscapeCharacterSet()) {
            printAndEscape(value, offset, len);
        } else {
            out.append(value, offset, offset + len);
        }
        newRecord = false;
    }