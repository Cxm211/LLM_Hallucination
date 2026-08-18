public Iterator<Chromosome> iterator() {
    if (chromosomes == null) {
        return Collections.emptyIterator();
    }
    return chromosomes.iterator();
}