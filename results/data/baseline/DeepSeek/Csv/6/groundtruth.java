// ===== FIXED org.apache.commons.csv.CSVRecord :: putIn(M) [lines 179-187] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-6-fixed/src/main/java/org/apache/commons/csv/CSVRecord.java =====
    <M extends Map<String, String>> M putIn(final M map) {
        for (final Entry<String, Integer> entry : mapping.entrySet()) {
            final int col = entry.getValue().intValue();
            if (col < values.length) {
                map.put(entry.getKey(), values[col]);
            }
        }
        return map;
    }
