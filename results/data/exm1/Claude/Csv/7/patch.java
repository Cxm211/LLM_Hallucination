private Map<String, Integer> initializeHeader() throws IOException {
    Map<String, Integer> hdrMap = null;
    final String[] formatHeader = this.format.getHeader();
    if (formatHeader != null) {
        hdrMap = new LinkedHashMap<String, Integer>();

        String[] header = null;
        if (formatHeader.length == 0) {
            // read the header from the first line of the file
            final CSVRecord nextRecord = this.nextRecord();
            if (nextRecord != null) {
                header = nextRecord.values();
            }
        } else {
            if (this.format.getSkipHeaderRecord()) {
                this.nextRecord();
            }
            header = formatHeader;
        }

        // build the name to index mappings
        if (header != null) {
            for (int i = 0; i < header.length; i++) {
                final String headerName = header[i];
                if (hdrMap.containsKey(headerName)) {
                    throw new IllegalArgumentException("The header contains a duplicate name: \"" + headerName + "\" in " + Arrays.toString(header));
                }
                hdrMap.put(headerName, Integer.valueOf(i));
            }
        }
    }
    return hdrMap;
}