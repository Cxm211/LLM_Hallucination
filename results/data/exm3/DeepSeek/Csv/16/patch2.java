    public Iterator<CSVRecord> iterator() {
        return new Iterator<CSVRecord>() {
        private CSVRecord getNextRecord() {
            try {
                return CSVParser.this.nextRecord();
            } catch (final IOException e) {
                throw new IllegalStateException(
                        e.getClass().getSimpleName() + " reading next record: " + e.toString(), e);
            }
        }
  
        @Override
        public boolean hasNext() {
            if (CSVParser.this.isClosed()) {
                return false;
            }
            if (CSVParser.this.peekedRecord == null) {
                CSVParser.this.peekedRecord = this.getNextRecord();
            }
            return CSVParser.this.peekedRecord != null;
        }
  
        @Override
        public CSVRecord next() {
            if (CSVParser.this.isClosed()) {
                throw new NoSuchElementException("CSVParser has been closed");
            }
            CSVRecord next = CSVParser.this.peekedRecord;
            if (next == null) {
                next = this.getNextRecord();
            } else {
                CSVParser.this.peekedRecord = null;
            }
            if (next == null) {
                throw new NoSuchElementException("No more CSV records available");
            }
            return next;
        }
  
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };
    }