// org/apache/commons/math3/genetics/ListPopulationTest.java
@Test
public void testIteratorPartialRemoval() {
    final ArrayList<Chromosome> chromosomes = new ArrayList<Chromosome>();
    chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
    chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
    chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));
    chromosomes.add(new DummyBinaryChromosome(BinaryChromosome.randomBinaryRepresentation(3)));

    final ListPopulation population = new ListPopulation(10) {
        public Population nextGeneration() {
            return null;
        }
    };

    population.addChromosomes(chromosomes);

    final Iterator<Chromosome> iter = population.iterator();
    iter.next();
    iter.remove();
    iter.next();
    iter.remove();
    
    Assert.assertEquals(4, population.getPopulationSize());
}