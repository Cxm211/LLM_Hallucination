// ===== FIXED org.apache.commons.csv.CSVParser :: CSVParser [lines 324-326] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-16-fixed/src/main/java/org/apache/commons/csv/CSVParser.java =====
    public CSVParser(final Reader reader, final CSVFormat format) throws IOException {
        this(reader, format, 0, 1);
    }

// ===== FIXED org.apache.commons.csv.CSVParser :: iterator() [lines 524-526] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-16-fixed/src/main/java/org/apache/commons/csv/CSVParser.java =====
    public Iterator<CSVRecord> iterator() {
        return csvRecordIterator;
    }
