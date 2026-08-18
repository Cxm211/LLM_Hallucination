public Iterator<Chromosome> iterator() {
    return java.util.Collections.unmodifiableList(this.chromosomes).iterator();
}