// org/apache/commons/math/fraction/FractionTest.java::testCompareTo
        Fraction npi1 = new Fraction(-1068966896, 340262731);
        Fraction npi2 = new Fraction(-411557987, 131002976);
        assertEquals(1, npi1.compareTo(npi2));
        assertEquals(-1, npi2.compareTo(npi1));
        assertEquals(0.0, npi1.doubleValue() - npi2.doubleValue(), 1.0e-20);
