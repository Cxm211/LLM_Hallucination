// ===== FIXED org.apache.commons.csv.CSVParser :: getHeaderMap() [lines 287-289] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-4-fixed/src/main/java/org/apache/commons/csv/CSVParser.java =====
    public Map<String, Integer> getHeaderMap() {
        return this.headerMap == null ? null : new LinkedHashMap<String, Integer>(this.headerMap);
    }
