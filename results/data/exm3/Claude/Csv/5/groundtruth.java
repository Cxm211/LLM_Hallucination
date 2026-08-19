// ===== FIXED org.apache.commons.csv.CSVPrinter :: println() [lines 323-329] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-5-fixed/src/main/java/org/apache/commons/csv/CSVPrinter.java =====
    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
        if (recordSeparator != null) {
            out.append(recordSeparator);
        }
        newRecord = true;
    }
