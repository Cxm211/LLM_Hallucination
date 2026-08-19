// ===== FIXED org.apache.commons.csv.CSVRecord :: get(String) [lines 79-93] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-2-fixed/src/main/java/org/apache/commons/csv/CSVRecord.java =====
    public String get(final String name) {
        if (mapping == null) {
            throw new IllegalStateException(
                    "No header mapping was specified, the record values can't be accessed by name");
        }
        final Integer index = mapping.get(name);
        try {
            return index != null ? values[index.intValue()] : null;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Index for header '%s' is %d but CSVRecord only has %d values!",
                            name, index.intValue(), values.length));
        }
    }
