public Iterator<CSVRecord> iterator() {
        // Cache a single iterator instance per parser to avoid advancing the parser state
        // when a throwaway iterator's hasNext() is called.
        if (this.iterator != null) {
            return this.iterator;
        }
        this.iterator = new Iterator<CSVRecord>() {
        private CSVRecord current;
  
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
            if (this.current == null) {
                this.current = this.getNextRecord();
            }
  
            return this.current != null;
        }
  
        @Override
        public CSVRecord next() {
            if (CSVParser.this.isClosed()) {
                throw new NoSuchElementException("CSVParser has been closed");
            }
            CSVRecord next = this.current;
            this.current = null;
  
            if (next == null) {
                // hasNext() wasn't called before
                next = this.getNextRecord();
                if (next == null) {
                    throw new NoSuchElementException("No more CSV records available");
                }
            }
  
            return next;
        }
  
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    };
    return this.iterator;
    }