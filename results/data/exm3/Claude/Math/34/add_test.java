// org/apache/commons/math3/genetics/ListPopulationTest.java
@Test
public void testIteratorMultipleIterators() {
    final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
    chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
    chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
    chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));

    final ListPopulation population = new ListPopulation(10) {
        public Population nextGeneration() {
            return null;
        }
    };

    population.addChromosomes(chromosomes);

    final Iterator<Chromosome> iter1 = population.iterator();
    final Iterator<Chromosome> iter2 = population.iterator();
    
    iter1.next();
    iter1.remove();
    
    int count = 0;
    while (iter2.hasNext()) {
        iter2.next();
        count++;
    }
    
    Assert.assertEquals(3, count);
}