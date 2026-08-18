public Iterator<Chromosome> iterator() {
    return new Iterator<Chromosome>() {
        private final Iterator<Chromosome> delegate = chromosomes.iterator();

        public boolean hasNext() {
            return delegate.hasNext();
        }

        public Chromosome next() {
            return delegate.next();
        }

        public void remove() {
            delegate.remove();
            populationLimit--;
        }
    };
}