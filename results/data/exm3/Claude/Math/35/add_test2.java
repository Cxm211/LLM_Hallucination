// org/apache/commons/math3/genetics/ElitisticListPopulationTest.java
@Test(expected = IllegalArgumentException.class)
public void testChromosomeListConstructorJustAboveOne() {
    final List<Chromosome> chromosomes = Collections.emptyList();
    final double rate = 1.0001;
    new ElitisticListPopulation(chromosomes, 100, rate);
}