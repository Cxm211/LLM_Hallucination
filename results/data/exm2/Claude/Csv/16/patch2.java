public Iterator<CSVRecord> iterator() {
        return new Iterator<CSVRecord>() {
        private CSVRecord current;
        private boolean getNextCalled = false;
  
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
            if (this.current == null && !this.getNextCalled) {
                this.current = this.getNextRecord();
                this.getNextCalled = true;
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
            this.getNextCalled = false;
  
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
    }