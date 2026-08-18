private void print(final Object object, final CharSequence value, final int offset, final int len) throws IOException {
    if (!newRecord) {
        out.append(format.getDelimiter());
    }
    if (value == null) {
        final String nullString = format.getNullString();
        out.append(nullString != null ? nullString : "");
    } else if (format.isQuoteCharacterSet()) {
        printAndQuote(object, value, offset, len);
    } else if (format.isEscapeCharacterSet()) {
        printAndEscape(value, offset, len);
    } else {
        out.append(value, offset, offset + len);
    }
    newRecord = false;
}