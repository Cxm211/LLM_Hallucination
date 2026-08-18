private Map<String, Integer> initializeHeader() throws IOException {
        Map<String, Integer> hdrMap = null;
        final String[] formatHeader = this.format.getHeader();
        if (formatHeader != null) {
            final String[] header;
            if (formatHeader.length == 0) {
                final CSVRecord nextRecord = this.nextRecord();
                if (nextRecord != null) {
                    header = nextRecord.values();
                    if (header.length > 0 && header[0] != null && header[0].startsWith("\uFEFF")) {
                        header[0] = header[0].substring(1);
                    }
                } else {
                    return null;
                }
            } else {
                if (this.format.getSkipHeaderRecord()) {
                    this.nextRecord();
                }
                header = formatHeader;
            }
            hdrMap = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < header.length; i++) {
                hdrMap.put(header[i], Integer.valueOf(i));
            }
        }
        return hdrMap;
    }