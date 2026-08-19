// ===== FIXED org.apache.commons.csv.ExtendedBufferedReader :: read() [lines 56-63] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Csv/Csv-1-fixed/src/main/java/org/apache/commons/csv/ExtendedBufferedReader.java =====
    public int read() throws IOException {
        int current = super.read();
        if (current == '\r' || (current == '\n' && lastChar != '\r')) {
            lineCounter++;
        }
        lastChar = current;
        return lastChar;
    }
