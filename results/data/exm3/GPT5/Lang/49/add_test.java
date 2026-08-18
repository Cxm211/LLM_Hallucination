// org/apache/commons/lang/math/FractionTest.java::testReduce
        f = Fraction.getFraction(0, -10);
        result = f.reduce();
        assertSame(result, Fraction.ZERO);
