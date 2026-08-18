// buggy code
    private BigFraction(final double value, final double epsilon,
                        final int maxDenominator, int maxIterations)
        throws FractionConversionException {
        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long) FastMath.floor(r0);
        if (a0 > overflow) {
            throw new FractionConversionException(value, a0, 1l);
        }

        // check for (almost) integer arguments, which should not go
        // to iterations.
        if (FastMath.abs(a0 - value) < epsilon) {
            numerator = BigInteger.valueOf(a0);
            denominator = BigInteger.ONE;
            return;
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2 = 0;
        long q2 = 1;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            final double r1 = 1.0 / (r0 - a0);
            final long a1 = (long) FastMath.floor(r1);
            p2 = (a1 * p1) + p0;
            q2 = (a1 * q1) + q0;
            if ((p2 > overflow) || (q2 > overflow)) {
                // in maxDenominator mode, if the last fraction was very close to the actual value
                // q2 may overflow in the next iteration; in this case return the last one.
                throw new FractionConversionException(value, p2, q2);
            }

            final double convergent = (double) p2 / (double) q2;
            if ((n < maxIterations) &&
                (FastMath.abs(convergent - value) > epsilon) &&
                (q2 < maxDenominator)) {
                p0 = p1;
                p1 = p2;
                q0 = q1;
                q1 = q2;
                a0 = a1;
                r0 = r1;
            } else {
                stop = true;
            }
        } while (!stop);

        if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
        }

        if (q2 < maxDenominator) {
            numerator   = BigInteger.valueOf(p2);
            denominator = BigInteger.valueOf(q2);
        } else {
            numerator   = BigInteger.valueOf(p1);
            denominator = BigInteger.valueOf(q1);
        }
    }

    private Fraction(double value, double epsilon, int maxDenominator, int maxIterations)
        throws FractionConversionException
    {
        long overflow = Integer.MAX_VALUE;
        double r0 = value;
        long a0 = (long)FastMath.floor(r0);
        if (FastMath.abs(a0) > overflow) {
            throw new FractionConversionException(value, a0, 1l);
        }

        // check for (almost) integer arguments, which should not go to iterations.
        if (FastMath.abs(a0 - value) < epsilon) {
            this.numerator = (int) a0;
            this.denominator = 1;
            return;
        }

        long p0 = 1;
        long q0 = 0;
        long p1 = a0;
        long q1 = 1;

        long p2 = 0;
        long q2 = 1;

        int n = 0;
        boolean stop = false;
        do {
            ++n;
            double r1 = 1.0 / (r0 - a0);
            long a1 = (long)FastMath.floor(r1);
            p2 = (a1 * p1) + p0;
            q2 = (a1 * q1) + q0;

            if ((FastMath.abs(p2) > overflow) || (FastMath.abs(q2) > overflow)) {
                // in maxDenominator mode, if the last fraction was very close to the actual value
                // q2 may overflow in the next iteration; in this case return the last one.
                throw new FractionConversionException(value, p2, q2);
            }

            double convergent = (double)p2 / (double)q2;
            if (n < maxIterations && FastMath.abs(convergent - value) > epsilon && q2 < maxDenominator) {
                p0 = p1;
                p1 = p2;
                q0 = q1;
                q1 = q2;
                a0 = a1;
                r0 = r1;
            } else {
                stop = true;
            }
        } while (!stop);

        if (n >= maxIterations) {
            throw new FractionConversionException(value, maxIterations);
        }

        if (q2 < maxDenominator) {
            this.numerator = (int) p2;
            this.denominator = (int) q2;
        } else {
            this.numerator = (int) p1;
            this.denominator = (int) q1;
        }

    }

// relevant test
// org.apache.commons.math3.util.OpenIntToFieldTest::testPutOnExisting
    public void testPutOnExisting() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.put(mapEntry.getKey(), mapEntry.getValue());
            Assert.assertEquals(javaMap.size(), map.size());
            Assert.assertEquals(mapEntry.getValue(), map.get(mapEntry.getKey()));
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testGetAbsent
    public void testGetAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet())
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testGetFromEmpty
    public void testGetFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        Assert.assertTrue(field.getZero().equals(map.get(5)));
        Assert.assertTrue(field.getZero().equals(map.get(0)));
        Assert.assertTrue(field.getZero().equals(map.get(50)));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemove
    public void testRemove() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        Assert.assertEquals(mapSize, map.size());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            map.remove(mapEntry.getKey());
            Assert.assertEquals(--mapSize, map.size());
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }

        
        assertPutAndGet(map);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemove2
    public void testRemove2() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = javaMap.size();
        int count = 0;
        Set<Integer> keysInMap = new HashSet<Integer>(javaMap.keySet());
        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            keysInMap.remove(mapEntry.getKey());
            map.remove(mapEntry.getKey());
            Assert.assertEquals(--mapSize, map.size());
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
            if (count++ > 5)
                break;
        }

        
        assertPutAndGet(map, mapSize, keysInMap);
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemoveFromEmpty
    public void testRemoveFromEmpty() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        Assert.assertTrue(field.getZero().equals(map.remove(50)));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testRemoveAbsent
    public void testRemoveAbsent() {
        Map<Integer, Fraction> generated = generateAbsent();

        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        int mapSize = map.size();

        for (Map.Entry<Integer, Fraction> mapEntry : generated.entrySet()) {
            map.remove(mapEntry.getKey());
            Assert.assertEquals(mapSize, map.size());
            Assert.assertTrue(field.getZero().equals(map.get(mapEntry.getKey())));
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testCopy
    public void testCopy() {
        OpenIntToFieldHashMap<Fraction> copy =
            new OpenIntToFieldHashMap<Fraction>(createFromJavaMap(field));
        Assert.assertEquals(javaMap.size(), copy.size());

        for (Map.Entry<Integer, Fraction> mapEntry : javaMap.entrySet())
            Assert.assertEquals(mapEntry.getValue(), copy.get(mapEntry.getKey()));
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testContainsKey
    public void testContainsKey() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            Assert.assertTrue(map.containsKey(mapEntry.getKey()));
        }
        for (Map.Entry<Integer, Fraction> mapEntry : generateAbsent().entrySet()) {
            Assert.assertFalse(map.containsKey(mapEntry.getKey()));
        }
        for (Entry<Integer, Fraction> mapEntry : javaMap.entrySet()) {
            int key = mapEntry.getKey();
            Assert.assertTrue(map.containsKey(key));
            map.remove(key);
            Assert.assertFalse(map.containsKey(key));
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testIterator
    public void testIterator() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        for (int i = 0; i < map.size(); ++i) {
            Assert.assertTrue(iterator.hasNext());
            iterator.advance();
            int key = iterator.key();
            Assert.assertTrue(map.containsKey(key));
            Assert.assertEquals(javaMap.get(key), map.get(key));
            Assert.assertEquals(javaMap.get(key), iterator.value());
            Assert.assertTrue(javaMap.containsKey(key));
        }
        Assert.assertFalse(iterator.hasNext());
        try {
            iterator.advance();
            Assert.fail("an exception should have been thrown");
        } catch (NoSuchElementException nsee) {
            
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testConcurrentModification
    public void testConcurrentModification() {
        OpenIntToFieldHashMap<Fraction> map = createFromJavaMap(field);
        OpenIntToFieldHashMap<Fraction>.Iterator iterator = map.iterator();
        map.put(3, new Fraction(3));
        try {
            iterator.advance();
            Assert.fail("an exception should have been thrown");
        } catch (ConcurrentModificationException cme) {
            
        }
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutKeysWithCollisions
    public void testPutKeysWithCollisions() {
        OpenIntToFieldHashMap<Fraction> map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = -1996012590;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 835099822;
        map.put(key2, value1);
        int key3 = 1008859686;
        map.put(key3, value1);
        Assert.assertEquals(value1, map.get(key3));
        Assert.assertEquals(3, map.size());

        map.remove(key2);
        Fraction value2 = new Fraction(2);
        map.put(key3, value2);
        Assert.assertEquals(value2, map.get(key3));
        Assert.assertEquals(2, map.size());
    }

// org.apache.commons.math3.util.OpenIntToFieldTest::testPutKeysWithCollision2
    public void testPutKeysWithCollision2() {
        OpenIntToFieldHashMap<Fraction>map = new OpenIntToFieldHashMap<Fraction>(field);
        int key1 = 837989881;
        Fraction value1 = new Fraction(1);
        map.put(key1, value1);
        int key2 = 476463321;
        map.put(key2, value1);
        Assert.assertEquals(2, map.size());
        Assert.assertEquals(value1, map.get(key2));

        map.remove(key1);
        Fraction value2 = new Fraction(2);
        map.put(key2, value2);
        Assert.assertEquals(1, map.size());
        Assert.assertEquals(value2, map.get(key2));
    }
