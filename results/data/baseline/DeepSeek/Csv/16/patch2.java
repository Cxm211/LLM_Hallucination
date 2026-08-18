public Iterator<CSVRecord> iterator() {
    return new Iterator<CSVRecord>() {
        private CSVRecord current;
        private boolean noMoreRecords = false;

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
            if (CSVParser.this.isClosed() || noMoreRecords) {
                return false;
            }
            if (this.current == null) {
                this.current = this.getNextRecord();
                if (this.current == null) {
                    noMoreRecords = true;
                    return false;
                }
            }
            return true;
        }

        @Override
        public CSVRecord next() {
            if (CSVParser.this.isClosed()) {
                throw new NoSuchElementException("CSVParser has been closed");
            }
            if (noMoreRecords && this.current == null) {
                throw new NoSuchElementException("No more CSV records available");
            }
            CSVRecord next = this.current;
            this.current = null;

            if (next == null) {
                next = this.getNextRecord();
                if (next == null) {
                    noMoreRecords = true;
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