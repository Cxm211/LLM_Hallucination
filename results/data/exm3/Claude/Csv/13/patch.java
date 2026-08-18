private void print(final Object object, final CharSequence value, final int offset, final int len)
        throws IOException {
    if (!newRecord) {
        out.append(format.getDelimiter());
    }
    if (object == null && format.getNullString() != null && value != null && 
        value.length() == len && format.getNullString().contentEquals(value.subSequence(offset, offset + len))) {
        // Printing null string representation - don't quote or escape
        out.append(value, offset, offset + len);
    } else if (format.isQuoteCharacterSet()) {
        // the original object is needed so can check for Number
        printAndQuote(object, value, offset, len);
    } else if (format.isEscapeCharacterSet()) {
        printAndEscape(value, offset, len);
    } else {
        out.append(value, offset, offset + len);
    }
    newRecord = false;
}