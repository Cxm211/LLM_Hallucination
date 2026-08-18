public Iterator<Chromosome> iterator() {
        // Create a snapshot iterator to iterate over a stable copy while allowing removals
        final Iterator<Chromosome> snapshot = new ArrayList<Chromosome>(chromosomes).iterator();
        return new Iterator<Chromosome>() {
            private Chromosome lastReturned = null;
            public boolean hasNext() {
                return snapshot.hasNext();
            }
            public Chromosome next() {
                lastReturned = snapshot.next();
                return lastReturned;
            }
            public void remove() {
                if (lastReturned == null) {
                    throw new IllegalStateException();
                }
                chromosomes.remove(lastReturned);
                lastReturned = null;
            }
        };
    }