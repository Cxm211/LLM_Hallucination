public Iterator<CSVRecord> iterator() {
    return new Iterator<CSVRecord>() {
    private CSVRecord current;
    private boolean currentConsumed = true;

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
        if (this.currentConsumed) {
            this.current = this.getNextRecord();
            this.currentConsumed = false;
        }

        return this.current != null;
    }

    @Override
    public CSVRecord next() {
        if (CSVParser.this.isClosed()) {
            throw new NoSuchElementException("CSVParser has been closed");
        }
        if (this.currentConsumed) {
            this.current = this.getNextRecord();
            if (this.current == null) {
                throw new NoSuchElementException("No more CSV records available");
            }
        }
        this.currentConsumed = true;
        return this.current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
};
}