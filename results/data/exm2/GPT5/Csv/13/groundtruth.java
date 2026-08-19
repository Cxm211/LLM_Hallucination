// ===== FIXED org.apache.commons.csv.CSVPrinter :: print(Object, CharSequence, int, int) [lines 134-150] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-13-fixed/src/main/java/org/apache/commons/csv/CSVPrinter.java =====
    private void print(final Object object, final CharSequence value, final int offset, final int len)
            throws IOException {
        if (!newRecord) {
            out.append(format.getDelimiter());
        }
        if (object == null) {
            out.append(value);
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
