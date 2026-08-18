    public void println() throws IOException {
        final String recordSeparator = format.getRecordSeparator();
        if (recordSeparator == null) {
            out.append(System.lineSeparator());
        } else {
            out.append(recordSeparator);
        }
        newRecord = true;
    }