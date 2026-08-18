// buggy code
    public static <E> BoundedCollection<E> unmodifiableBoundedCollection(final BoundedCollection<? extends E> coll) {
        return new UnmodifiableBoundedCollection<E>(coll);
    }

// relevant test
// org.apache.commons.collections4.CollectionUtilsTest::getCardinalityMap
    public void getCardinalityMap() {
        final Map<Number, Integer> freqA = CollectionUtils.<Number>getCardinalityMap(iterableA);
        assertEquals(1, (int) freqA.get(1));
        assertEquals(2, (int) freqA.get(2));
        assertEquals(3, (int) freqA.get(3));
        assertEquals(4, (int) freqA.get(4));
        assertNull(freqA.get(5));

        final Map<Long, Integer> freqB = CollectionUtils.getCardinalityMap(iterableB);
        assertNull(freqB.get(1L));
        assertEquals(4, (int) freqB.get(2L));
        assertEquals(3, (int) freqB.get(3L));
        assertEquals(2, (int) freqB.get(4L));
        assertEquals(1, (int) freqB.get(5L));
    }

// org.apache.commons.collections4.CollectionUtilsTest::cardinality
    public void cardinality() {
        assertEquals(1, CollectionUtils.cardinality(1, iterableA));
        assertEquals(2, CollectionUtils.cardinality(2, iterableA));
        assertEquals(3, CollectionUtils.cardinality(3, iterableA));
        assertEquals(4, CollectionUtils.cardinality(4, iterableA));
        assertEquals(0, CollectionUtils.cardinality(5, iterableA));

        assertEquals(0, CollectionUtils.cardinality(1L, iterableB));
        assertEquals(4, CollectionUtils.cardinality(2L, iterableB));
        assertEquals(3, CollectionUtils.cardinality(3L, iterableB));
        assertEquals(2, CollectionUtils.cardinality(4L, iterableB));
        assertEquals(1, CollectionUtils.cardinality(5L, iterableB));

        
        
        
        assertEquals(0, CollectionUtils.cardinality(2L, iterableA2));
        assertEquals(0, CollectionUtils.cardinality(2, iterableB2));

        final Set<String> set = new HashSet<String>();
        set.add("A");
        set.add("C");
        set.add("E");
        set.add("E");
        assertEquals(1, CollectionUtils.cardinality("A", set));
        assertEquals(0, CollectionUtils.cardinality("B", set));
        assertEquals(1, CollectionUtils.cardinality("C", set));
        assertEquals(0, CollectionUtils.cardinality("D", set));
        assertEquals(1, CollectionUtils.cardinality("E", set));

        final Bag<String> bag = new HashBag<String>();
        bag.add("A", 3);
        bag.add("C");
        bag.add("E");
        bag.add("E");
        assertEquals(3, CollectionUtils.cardinality("A", bag));
        assertEquals(0, CollectionUtils.cardinality("B", bag));
        assertEquals(1, CollectionUtils.cardinality("C", bag));
        assertEquals(0, CollectionUtils.cardinality("D", bag));
        assertEquals(2, CollectionUtils.cardinality("E", bag));
    }

// org.apache.commons.collections4.CollectionUtilsTest::cardinalityOfNull
    public void cardinalityOfNull() {
        final List<String> list = new ArrayList<String>();
        assertEquals(0, CollectionUtils.cardinality(null, list));
        {
            final Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertNull(freq.get(null));
        }
        list.add("A");
        assertEquals(0, CollectionUtils.cardinality(null, list));
        {
            final Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertNull(freq.get(null));
        }
        list.add(null);
        assertEquals(1, CollectionUtils.cardinality(null, list));
        {
            final Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(1), freq.get(null));
        }
        list.add("B");
        assertEquals(1, CollectionUtils.cardinality(null, list));
        {
            final Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(1), freq.get(null));
        }
        list.add(null);
        assertEquals(2, CollectionUtils.cardinality(null, list));
        {
            final Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(2), freq.get(null));
        }
        list.add("B");
        assertEquals(2, CollectionUtils.cardinality(null, list));
        {
            final Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(2), freq.get(null));
        }
        list.add(null);
        assertEquals(3, CollectionUtils.cardinality(null, list));
        {
            final Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(3), freq.get(null));
        }
    }

// org.apache.commons.collections4.CollectionUtilsTest::containsAll
    public void containsAll() {
        final Collection<String> empty = new ArrayList<String>(0);
        final Collection<String> one = new ArrayList<String>(1);
        one.add("1");
        final Collection<String> two = new ArrayList<String>(1);
        two.add("2");
        final Collection<String> three = new ArrayList<String>(1);
        three.add("3");
        final Collection<String> odds = new ArrayList<String>(2);
        odds.add("1");
        odds.add("3");
        final Collection<String> multiples = new ArrayList<String>(3);
        multiples.add("1");
        multiples.add("3");
        multiples.add("1");

        assertTrue("containsAll({1},{1,3}) should return false.", !CollectionUtils.containsAll(one, odds));
        assertTrue("containsAll({1,3},{1}) should return true.", CollectionUtils.containsAll(odds, one));
        assertTrue("containsAll({3},{1,3}) should return false.", !CollectionUtils.containsAll(three, odds));
        assertTrue("containsAll({1,3},{3}) should return true.", CollectionUtils.containsAll(odds, three));
        assertTrue("containsAll({2},{2}) should return true.", CollectionUtils.containsAll(two, two));
        assertTrue("containsAll({1,3},{1,3}) should return true.", CollectionUtils.containsAll(odds, odds));

        assertTrue("containsAll({2},{1,3}) should return false.", !CollectionUtils.containsAll(two, odds));
        assertTrue("containsAll({1,3},{2}) should return false.", !CollectionUtils.containsAll(odds, two));
        assertTrue("containsAll({1},{3}) should return false.", !CollectionUtils.containsAll(one, three));
        assertTrue("containsAll({3},{1}) should return false.", !CollectionUtils.containsAll(three, one));
        assertTrue("containsAll({1,3},{}) should return true.", CollectionUtils.containsAll(odds, empty));
        assertTrue("containsAll({},{1,3}) should return false.", !CollectionUtils.containsAll(empty, odds));
        assertTrue("containsAll({},{}) should return true.", CollectionUtils.containsAll(empty, empty));

        assertTrue("containsAll({1,3},{1,3,1}) should return true.", CollectionUtils.containsAll(odds, multiples));
        assertTrue("containsAll({1,3,1},{1,3,1}) should return true.", CollectionUtils.containsAll(odds, odds));
    }

// org.apache.commons.collections4.CollectionUtilsTest::containsAny
    public void containsAny() {
        final Collection<String> empty = new ArrayList<String>(0);
        final Collection<String> one = new ArrayList<String>(1);
        one.add("1");
        final Collection<String> two = new ArrayList<String>(1);
        two.add("2");
        final Collection<String> three = new ArrayList<String>(1);
        three.add("3");
        final Collection<String> odds = new ArrayList<String>(2);
        odds.add("1");
        odds.add("3");

        assertTrue("containsAny({1},{1,3}) should return true.", CollectionUtils.containsAny(one, odds));
        assertTrue("containsAny({1,3},{1}) should return true.", CollectionUtils.containsAny(odds, one));
        assertTrue("containsAny({3},{1,3}) should return true.", CollectionUtils.containsAny(three, odds));
        assertTrue("containsAny({1,3},{3}) should return true.", CollectionUtils.containsAny(odds, three));
        assertTrue("containsAny({2},{2}) should return true.", CollectionUtils.containsAny(two, two));
        assertTrue("containsAny({1,3},{1,3}) should return true.", CollectionUtils.containsAny(odds, odds));

        assertTrue("containsAny({2},{1,3}) should return false.", !CollectionUtils.containsAny(two, odds));
        assertTrue("containsAny({1,3},{2}) should return false.", !CollectionUtils.containsAny(odds, two));
        assertTrue("containsAny({1},{3}) should return false.", !CollectionUtils.containsAny(one, three));
        assertTrue("containsAny({3},{1}) should return false.", !CollectionUtils.containsAny(three, one));
        assertTrue("containsAny({1,3},{}) should return false.", !CollectionUtils.containsAny(odds, empty));
        assertTrue("containsAny({},{1,3}) should return false.", !CollectionUtils.containsAny(empty, odds));
        assertTrue("containsAny({},{}) should return false.", !CollectionUtils.containsAny(empty, empty));
    }

// org.apache.commons.collections4.CollectionUtilsTest::union
    public void union() {
        final Collection<Integer> col = CollectionUtils.union(iterableA, iterableC);
        final Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq.get(1));
        assertEquals(Integer.valueOf(4), freq.get(2));
        assertEquals(Integer.valueOf(3), freq.get(3));
        assertEquals(Integer.valueOf(4), freq.get(4));
        assertEquals(Integer.valueOf(1), freq.get(5));

        final Collection<Number> col2 = CollectionUtils.union(collectionC2, iterableA);
        final Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(Integer.valueOf(1), freq2.get(1));
        assertEquals(Integer.valueOf(4), freq2.get(2));
        assertEquals(Integer.valueOf(3), freq2.get(3));
        assertEquals(Integer.valueOf(4), freq2.get(4));
        assertEquals(Integer.valueOf(1), freq2.get(5));
    }

// org.apache.commons.collections4.CollectionUtilsTest::intersection
    public void intersection() {
        final Collection<Integer> col = CollectionUtils.intersection(iterableA, iterableC);
        final Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertNull(freq.get(1));
        assertEquals(Integer.valueOf(2), freq.get(2));
        assertEquals(Integer.valueOf(3), freq.get(3));
        assertEquals(Integer.valueOf(2), freq.get(4));
        assertNull(freq.get(5));

        final Collection<Number> col2 = CollectionUtils.intersection(collectionC2, collectionA);
        final Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertNull(freq2.get(1));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertEquals(Integer.valueOf(3), freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(4));
        assertNull(freq2.get(5));
    }

// org.apache.commons.collections4.CollectionUtilsTest::disjunction
    public void disjunction() {
        final Collection<Integer> col = CollectionUtils.disjunction(iterableA, iterableC);
        final Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq.get(1));
        assertEquals(Integer.valueOf(2), freq.get(2));
        assertNull(freq.get(3));
        assertEquals(Integer.valueOf(2), freq.get(4));
        assertEquals(Integer.valueOf(1), freq.get(5));

        final Collection<Number> col2 = CollectionUtils.disjunction(collectionC2, collectionA);
        final Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(Integer.valueOf(1), freq2.get(1));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertNull(freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(4));
        assertEquals(Integer.valueOf(1), freq2.get(5));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testDisjunctionAsUnionMinusIntersection
    public void testDisjunctionAsUnionMinusIntersection() {
        final Collection<Number> dis = CollectionUtils.<Number>disjunction(collectionA, collectionC);
        final Collection<Number> un = CollectionUtils.<Number>union(collectionA, collectionC);
        final Collection<Number> inter = CollectionUtils.<Number>intersection(collectionA, collectionC);
        assertTrue(CollectionUtils.isEqualCollection(dis, CollectionUtils.subtract(un, inter)));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testDisjunctionAsSymmetricDifference
    public void testDisjunctionAsSymmetricDifference() {
        final Collection<Number> dis = CollectionUtils.<Number>disjunction(collectionA, collectionC);
        final Collection<Number> amb = CollectionUtils.<Number>subtract(collectionA, collectionC);
        final Collection<Number> bma = CollectionUtils.<Number>subtract(collectionC, collectionA);
        assertTrue(CollectionUtils.isEqualCollection(dis, CollectionUtils.union(amb, bma)));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSubtract
    public void testSubtract() {
        final Collection<Integer> col = CollectionUtils.subtract(iterableA, iterableC);
        final Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq.get(1));
        assertNull(freq.get(2));
        assertNull(freq.get(3));
        assertEquals(Integer.valueOf(2), freq.get(4));
        assertNull(freq.get(5));

        final Collection<Number> col2 = CollectionUtils.subtract(collectionC2, collectionA);
        final Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(Integer.valueOf(1), freq2.get(5));
        assertNull(freq2.get(4));
        assertNull(freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertNull(freq2.get(1));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSubtractWithPredicate
    public void testSubtractWithPredicate() {
        
        final Predicate<Number> predicate = new Predicate<Number>() {
            public boolean evaluate(final Number n) {
                return n.longValue() > 3L;
            }
        };

        final Collection<Number> col = CollectionUtils.subtract(iterableA, collectionC, predicate);
        final Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq2.get(1));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertEquals(Integer.valueOf(3), freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(4));
        assertNull(freq2.get(5));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsSubCollectionOfSelf
    public void testIsSubCollectionOfSelf() {
        assertTrue(CollectionUtils.isSubCollection(collectionA, collectionA));
        assertTrue(CollectionUtils.isSubCollection(collectionB, collectionB));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsSubCollection
    public void testIsSubCollection() {
        assertTrue(!CollectionUtils.isSubCollection(collectionA, collectionC));
        assertTrue(!CollectionUtils.isSubCollection(collectionC, collectionA));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsSubCollection2
    public void testIsSubCollection2() {
        final Collection<Integer> c = new ArrayList<Integer>();
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(1);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(2);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(2);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(3);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(3);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(3);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(4);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(4);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(4);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA, c));
        c.add(4);
        assertTrue(CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(CollectionUtils.isSubCollection(collectionA, c));
        c.add(5);
        assertTrue(!CollectionUtils.isSubCollection(c, collectionA));
        assertTrue(CollectionUtils.isSubCollection(collectionA, c));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEqualCollectionToSelf
    public void testIsEqualCollectionToSelf() {
        assertTrue(CollectionUtils.isEqualCollection(collectionA, collectionA));
        assertTrue(CollectionUtils.isEqualCollection(collectionB, collectionB));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEqualCollection
    public void testIsEqualCollection() {
        assertTrue(!CollectionUtils.isEqualCollection(collectionA, collectionC));
        assertTrue(!CollectionUtils.isEqualCollection(collectionC, collectionA));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEqualCollectionReturnsFalse
    public void testIsEqualCollectionReturnsFalse() {
        final List<Integer> b = new ArrayList<Integer>(collectionA);
        
        b.remove(1);
        b.add(5);
        assertFalse(CollectionUtils.isEqualCollection(collectionA, b));
        assertFalse(CollectionUtils.isEqualCollection(b, collectionA));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEqualCollection2
    public void testIsEqualCollection2() {
        final Collection<String> a = new ArrayList<String>();
        final Collection<String> b = new ArrayList<String>();
        assertTrue(CollectionUtils.isEqualCollection(a, b));
        assertTrue(CollectionUtils.isEqualCollection(b, a));
        a.add("1");
        assertTrue(!CollectionUtils.isEqualCollection(a, b));
        assertTrue(!CollectionUtils.isEqualCollection(b, a));
        b.add("1");
        assertTrue(CollectionUtils.isEqualCollection(a, b));
        assertTrue(CollectionUtils.isEqualCollection(b, a));
        a.add("2");
        assertTrue(!CollectionUtils.isEqualCollection(a, b));
        assertTrue(!CollectionUtils.isEqualCollection(b, a));
        b.add("2");
        assertTrue(CollectionUtils.isEqualCollection(a, b));
        assertTrue(CollectionUtils.isEqualCollection(b, a));
        a.add("1");
        assertTrue(!CollectionUtils.isEqualCollection(a, b));
        assertTrue(!CollectionUtils.isEqualCollection(b, a));
        b.add("1");
        assertTrue(CollectionUtils.isEqualCollection(a, b));
        assertTrue(CollectionUtils.isEqualCollection(b, a));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEqualCollectionEquator
    public void testIsEqualCollectionEquator() {
        final Collection<Integer> collB = CollectionUtils.collect(collectionB, TRANSFORM_TO_INTEGER);

        
        final Equator<Integer> e = new Equator<Integer>() {
            public boolean equate(final Integer o1, final Integer o2) {
                if (o1.intValue() % 2 == 0 ^ o2.intValue() % 2 == 0) {
                    return false;
                } else {
                    return true;
                }
            }

            public int hash(final Integer o) {
                return o.intValue() % 2 == 0 ? Integer.valueOf(0).hashCode() : Integer.valueOf(1).hashCode();
            }
        };

        assertTrue(CollectionUtils.isEqualCollection(collectionA, collectionA, e));
        assertTrue(CollectionUtils.isEqualCollection(collectionA, collB, e));
        assertTrue(CollectionUtils.isEqualCollection(collB, collectionA, e));

        final Equator<Number> defaultEquator = DefaultEquator.defaultEquator();
        assertFalse(CollectionUtils.isEqualCollection(collectionA, collectionB, defaultEquator));
        assertFalse(CollectionUtils.isEqualCollection(collectionA, collB, defaultEquator));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEqualCollectionNullEquator
    public void testIsEqualCollectionNullEquator() {
        CollectionUtils.isEqualCollection(collectionA, collectionA, null);
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsProperSubCollection
    public void testIsProperSubCollection() {
        final Collection<String> a = new ArrayList<String>();
        final Collection<String> b = new ArrayList<String>();
        assertTrue(!CollectionUtils.isProperSubCollection(a, b));
        b.add("1");
        assertTrue(CollectionUtils.isProperSubCollection(a, b));
        assertTrue(!CollectionUtils.isProperSubCollection(b, a));
        assertTrue(!CollectionUtils.isProperSubCollection(b, b));
        assertTrue(!CollectionUtils.isProperSubCollection(a, a));
        a.add("1");
        a.add("2");
        b.add("2");
        assertTrue(!CollectionUtils.isProperSubCollection(b, a));
        assertTrue(!CollectionUtils.isProperSubCollection(a, b));
        a.add("1");
        assertTrue(CollectionUtils.isProperSubCollection(b, a));
        assertTrue(CollectionUtils.isProperSubCollection(CollectionUtils.intersection(collectionA, collectionC), collectionA));
        assertTrue(CollectionUtils.isProperSubCollection(CollectionUtils.subtract(a, b), a));
        assertTrue(!CollectionUtils.isProperSubCollection(a, CollectionUtils.subtract(a, b)));
    }

// org.apache.commons.collections4.CollectionUtilsTest::find
    public void find() {
        Predicate<Number> testPredicate = equalPredicate((Number) 4);
        Integer test = CollectionUtils.find(collectionA, testPredicate);
        assertTrue(test.equals(4));
        testPredicate = equalPredicate((Number) 45);
        test = CollectionUtils.find(collectionA, testPredicate);
        assertTrue(test == null);
        assertNull(CollectionUtils.find(null,testPredicate));
        assertNull(CollectionUtils.find(collectionA, null));
    }

// org.apache.commons.collections4.CollectionUtilsTest::forAllDoCollection
    public void forAllDoCollection() {
        final Closure<List<? extends Number>> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<List<? extends Number>> col = new ArrayList<List<? extends Number>>();
        col.add(collectionA);
        col.add(collectionB);
        Closure<List<? extends Number>> resultClosure = CollectionUtils.forAllDo(col, testClosure);
        assertSame(testClosure, resultClosure);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        
        resultClosure = CollectionUtils.forAllDo(col, (Closure<List<? extends Number>>) null);
        assertNull(resultClosure);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        resultClosure = CollectionUtils.forAllDo((Collection) null, testClosure);
        col.add(null);
        
        CollectionUtils.forAllDo(col, testClosure);
    }

// org.apache.commons.collections4.CollectionUtilsTest::forAllDoIterator
    public void forAllDoIterator() {
        final Closure<List<? extends Number>> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<List<? extends Number>> col = new ArrayList<List<? extends Number>>();
        col.add(collectionA);
        col.add(collectionB);
        Closure<List<? extends Number>> resultClosure = CollectionUtils.forAllDo(col.iterator(), testClosure);
        assertSame(testClosure, resultClosure);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        
        resultClosure = CollectionUtils.forAllDo(col.iterator(), (Closure<List<? extends Number>>) null);
        assertNull(resultClosure);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        resultClosure = CollectionUtils.forAllDo((Iterator) null, testClosure);
        col.add(null);
        
        CollectionUtils.forAllDo(col.iterator(), testClosure);
    }

// org.apache.commons.collections4.CollectionUtilsTest::forAllDoFailure
    public void forAllDoFailure() {
        final Closure<String> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<String> col = new ArrayList<String>();
        col.add("x");
        CollectionUtils.forAllDo(col, testClosure);
    }

// org.apache.commons.collections4.CollectionUtilsTest::forAllButLastDoCollection
    public void forAllButLastDoCollection() {
        final Closure<List<? extends Number>> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<List<? extends Number>> col = new ArrayList<List<? extends Number>>();
        col.add(collectionA);
        col.add(collectionB);
        List<? extends Number> lastElement = CollectionUtils.forAllButLastDo(col, testClosure);
        assertSame(lastElement, collectionB);
        assertTrue(collectionA.isEmpty() && !collectionB.isEmpty());

        col.clear();
        col.add(collectionB);
        lastElement = CollectionUtils.forAllButLastDo(col, testClosure);
        assertSame(lastElement, collectionB);
        assertTrue(!collectionB.isEmpty() );

        col.clear();
        lastElement = CollectionUtils.forAllButLastDo(col, testClosure);
        assertNull(lastElement);

        Collection<String> strings = Arrays.asList(new String[]{"a", "b", "c"});
        final StringBuffer result = new StringBuffer();
        result.append(CollectionUtils.forAllButLastDo(strings, new Closure<String>() {
            public void execute(String input) {
                result.append(input+";");
            }
        }));
        assertEquals("a;b;c", result.toString());

        Collection<String> oneString = Arrays.asList(new String[]{"a"});
        final StringBuffer resultOne = new StringBuffer();
        resultOne.append(CollectionUtils.forAllButLastDo(oneString, new Closure<String>() {
            public void execute(String input) {
                resultOne.append(input+";");
            }
        }));
        assertEquals("a", resultOne.toString());
        assertNull(CollectionUtils.forAllButLastDo(strings, (Closure<String>) null));
        assertNull(CollectionUtils.forAllButLastDo((Collection<String>) null, (Closure<String>) null));
    }

// org.apache.commons.collections4.CollectionUtilsTest::forAllButLastDoIterator
    public void forAllButLastDoIterator() {
        final Closure<List<? extends Number>> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<List<? extends Number>> col = new ArrayList<List<? extends Number>>();
        col.add(collectionA);
        col.add(collectionB);
        List<? extends Number> lastElement = CollectionUtils.forAllButLastDo(col.iterator(), testClosure);
        assertSame(lastElement, collectionB);
        assertTrue(collectionA.isEmpty() && !collectionB.isEmpty());

        assertNull(CollectionUtils.forAllButLastDo(col.iterator(), (Closure<List<? extends Number>>) null));
        assertNull(CollectionUtils.forAllButLastDo((Iterator<String>) null, (Closure<String>) null));
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromMap
    public void getFromMap() {
        
        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("zeroKey", "zero");
        expected.put("oneKey", "one");

        final Map<String, String> found = new HashMap<String, String>();
        Map.Entry<String, String> entry = CollectionUtils.get(expected, 0);
        found.put(entry.getKey(), entry.getValue());
        entry = CollectionUtils.get(expected, 1);
        found.put(entry.getKey(), entry.getValue());
        assertEquals(expected, found);

        
        try {
            CollectionUtils.get(expected, 2);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException e) {
            
        }
        try {
            CollectionUtils.get(expected, -2);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException e) {
            
        }

        
        final SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("zeroKey", "zero");
        map.put("oneKey", "one");
        Map.Entry<String, String> test = CollectionUtils.get(map, 1);
        assertEquals("zeroKey", test.getKey());
        assertEquals("zero", test.getValue());
        test = CollectionUtils.get(map, 0);
        assertEquals("oneKey", test.getKey());
        assertEquals("one", test.getValue());
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromList
    public void getFromList() throws Exception {
        
        final List<String> list = createMock(List.class);
        expect(list.get(0)).andReturn("zero");
        expect(list.get(1)).andReturn("one");
        replay();
        final String string = CollectionUtils.get(list, 0);
        assertEquals("zero", string);
        assertEquals("one", CollectionUtils.get(list, 1));
        
        CollectionUtils.get(new ArrayList<Object>(), 2);
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromIterator
    public void getFromIterator() throws Exception {
        
        Iterator<Integer> iterator = iterableA.iterator();
        assertEquals(1, (int) CollectionUtils.get(iterator, 0));
        iterator = iterableA.iterator();
        assertEquals(2, (int) CollectionUtils.get(iterator, 1));

        
        try {
            CollectionUtils.get(iterator, 10);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException e) {
            
        }
        assertTrue(!iterator.hasNext());
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromEnumeration
    public void getFromEnumeration() throws Exception {
        
        final Vector<String> vector = new Vector<String>();
        vector.addElement("zero");
        vector.addElement("one");
        Enumeration<String> en = vector.elements();
        assertEquals("zero", CollectionUtils.get(en, 0));
        en = vector.elements();
        assertEquals("one", CollectionUtils.get(en, 1));

        
        try {
            CollectionUtils.get(en, 3);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException e) {
            
        }
        assertTrue(!en.hasMoreElements());
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromIterable
    public void getFromIterable() throws Exception {
        
        final Bag<String> bag = new HashBag<String>();
        bag.add("element", 1);
        assertEquals("element", CollectionUtils.get(bag, 0));

        
        CollectionUtils.get(bag, 1);
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromObjectArray
    public void getFromObjectArray() throws Exception {
        
        final Object[] objArray = new Object[2];
        objArray[0] = "zero";
        objArray[1] = "one";
        assertEquals("zero", CollectionUtils.get(objArray, 0));
        assertEquals("one", CollectionUtils.get(objArray, 1));

        
        
        CollectionUtils.get(objArray, 2);
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromPrimativeArray
    public void getFromPrimativeArray() throws Exception {
        
        final int[] array = new int[2];
        array[0] = 10;
        array[1] = 20;
        assertEquals(10, CollectionUtils.get(array, 0));
        assertEquals(20, CollectionUtils.get(array, 1));

        
        
        CollectionUtils.get(array, 2);
    }

// org.apache.commons.collections4.CollectionUtilsTest::getFromObject
    public void getFromObject() throws Exception {
        
        final Object obj = new Object();
        CollectionUtils.get(obj, 0);
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSize_List
    public void testSize_List() {
        List<String> list = null;
        assertEquals(0, CollectionUtils.size(list));
        list = new ArrayList<String>();
        assertEquals(0, CollectionUtils.size(list));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSize_Map
    public void testSize_Map() {
        final Map<String, String> map = new HashMap<String, String>();
        assertEquals(0, CollectionUtils.size(map));
        map.put("1", "a");
        assertEquals(1, CollectionUtils.size(map));
        map.put("2", "b");
        assertEquals(2, CollectionUtils.size(map));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSize_Array
    public void testSize_Array() {
        final Object[] objectArray = new Object[0];
        assertEquals(0, CollectionUtils.size(objectArray));

        final String[] stringArray = new String[3];
        assertEquals(3, CollectionUtils.size(stringArray));
        stringArray[0] = "a";
        stringArray[1] = "b";
        stringArray[2] = "c";
        assertEquals(3, CollectionUtils.size(stringArray));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSize_PrimitiveArray
    public void testSize_PrimitiveArray() {
        final int[] intArray = new int[0];
        assertEquals(0, CollectionUtils.size(intArray));

        final double[] doubleArray = new double[3];
        assertEquals(3, CollectionUtils.size(doubleArray));
        doubleArray[0] = 0.0d;
        doubleArray[1] = 1.0d;
        doubleArray[2] = 2.5d;
        assertEquals(3, CollectionUtils.size(doubleArray));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSize_Enumeration
    public void testSize_Enumeration() {
        final Vector<String> list = new Vector<String>();
        assertEquals(0, CollectionUtils.size(list.elements()));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list.elements()));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list.elements()));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSize_Iterator
    public void testSize_Iterator() {
        final List<String> list = new ArrayList<String>();
        assertEquals(0, CollectionUtils.size(list.iterator()));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list.iterator()));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list.iterator()));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSize_Other
    public void testSize_Other() {
        CollectionUtils.size("not a list");
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_Null
    public void testSizeIsEmpty_Null() {
        assertEquals(true, CollectionUtils.sizeIsEmpty(null));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_List
    public void testSizeIsEmpty_List() {
        final List<String> list = new ArrayList<String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_Map
    public void testSizeIsEmpty_Map() {
        final Map<String, String> map = new HashMap<String, String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(map));
        map.put("1", "a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(map));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_Array
    public void testSizeIsEmpty_Array() {
        final Object[] objectArray = new Object[0];
        assertEquals(true, CollectionUtils.sizeIsEmpty(objectArray));

        final String[] stringArray = new String[3];
        assertEquals(false, CollectionUtils.sizeIsEmpty(stringArray));
        stringArray[0] = "a";
        stringArray[1] = "b";
        stringArray[2] = "c";
        assertEquals(false, CollectionUtils.sizeIsEmpty(stringArray));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_PrimitiveArray
    public void testSizeIsEmpty_PrimitiveArray() {
        final int[] intArray = new int[0];
        assertEquals(true, CollectionUtils.sizeIsEmpty(intArray));

        final double[] doubleArray = new double[3];
        assertEquals(false, CollectionUtils.sizeIsEmpty(doubleArray));
        doubleArray[0] = 0.0d;
        doubleArray[1] = 1.0d;
        doubleArray[2] = 2.5d;
        assertEquals(false, CollectionUtils.sizeIsEmpty(doubleArray));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_Enumeration
    public void testSizeIsEmpty_Enumeration() {
        final Vector<String> list = new Vector<String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list.elements()));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list.elements()));
        final Enumeration<String> en = list.elements();
        en.nextElement();
        assertEquals(true, CollectionUtils.sizeIsEmpty(en));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_Iterator
    public void testSizeIsEmpty_Iterator() {
        final List<String> list = new ArrayList<String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list.iterator()));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list.iterator()));
        final Iterator<String> it = list.iterator();
        it.next();
        assertEquals(true, CollectionUtils.sizeIsEmpty(it));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSizeIsEmpty_Other
    public void testSizeIsEmpty_Other() {
        try {
            CollectionUtils.sizeIsEmpty("not a list");
            fail("Expecting IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
        }
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEmptyWithEmptyCollection
    public void testIsEmptyWithEmptyCollection() {
        final Collection<Object> coll = new ArrayList<Object>();
        assertEquals(true, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEmptyWithNonEmptyCollection
    public void testIsEmptyWithNonEmptyCollection() {
        final Collection<String> coll = new ArrayList<String>();
        coll.add("item");
        assertEquals(false, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsEmptyWithNull
    public void testIsEmptyWithNull() {
        final Collection<?> coll = null;
        assertEquals(true, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsNotEmptyWithEmptyCollection
    public void testIsNotEmptyWithEmptyCollection() {
        final Collection<Object> coll = new ArrayList<Object>();
        assertEquals(false, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsNotEmptyWithNonEmptyCollection
    public void testIsNotEmptyWithNonEmptyCollection() {
        final Collection<String> coll = new ArrayList<String>();
        coll.add("item");
        assertEquals(true, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections4.CollectionUtilsTest::testIsNotEmptyWithNull
    public void testIsNotEmptyWithNull() {
        final Collection<?> coll = null;
        assertEquals(false, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections4.CollectionUtilsTest::filter
    public void filter() {
        final List<Integer> ints = new ArrayList<Integer>();
        ints.add(1);
        ints.add(2);
        ints.add(3);
        ints.add(3);
        final Iterable<Integer> iterable = ints;
        assertTrue(CollectionUtils.filter(iterable, EQUALS_TWO));
        assertEquals(1, ints.size());
        assertEquals(2, (int) ints.get(0));
    }

// org.apache.commons.collections4.CollectionUtilsTest::filterNullParameters
    public void filterNullParameters() throws Exception {
        final List<Long> longs = Collections.nCopies(4, 10L);
        assertFalse(CollectionUtils.filter(longs, null));
        assertEquals(4, longs.size());
        assertFalse(CollectionUtils.filter(null, EQUALS_TWO));
        assertEquals(4, longs.size());
        assertFalse(CollectionUtils.filter(null, null));
        assertEquals(4, longs.size());
    }

// org.apache.commons.collections4.CollectionUtilsTest::filterInverse
    public void filterInverse() {
        final List<Integer> ints = new ArrayList<Integer>();
        ints.add(1);
        ints.add(2);
        ints.add(3);
        ints.add(3);
        final Iterable<Integer> iterable = ints;
        assertTrue(CollectionUtils.filterInverse(iterable, EQUALS_TWO));
        assertEquals(3, ints.size());
        assertEquals(1, (int) ints.get(0));
        assertEquals(3, (int) ints.get(1));
        assertEquals(3, (int) ints.get(2));
    }

// org.apache.commons.collections4.CollectionUtilsTest::filterInverseNullParameters
    public void filterInverseNullParameters() throws Exception {
        final List<Long> longs = Collections.nCopies(4, 10L);
        assertFalse(CollectionUtils.filterInverse(longs, null));
        assertEquals(4, longs.size());
        assertFalse(CollectionUtils.filterInverse(null, EQUALS_TWO));
        assertEquals(4, longs.size());
        assertFalse(CollectionUtils.filterInverse(null, null));
        assertEquals(4, longs.size());
    }

// org.apache.commons.collections4.CollectionUtilsTest::countMatches
    public void countMatches() {
        assertEquals(4, CollectionUtils.countMatches(iterableB, EQUALS_TWO));
        assertEquals(0, CollectionUtils.countMatches(iterableA, null));
        assertEquals(0, CollectionUtils.countMatches(null, EQUALS_TWO));
        assertEquals(0, CollectionUtils.countMatches(null, null));
    }

// org.apache.commons.collections4.CollectionUtilsTest::exists
    public void exists() {
        final List<Integer> list = new ArrayList<Integer>();
        assertFalse(CollectionUtils.exists(null, null));
        assertFalse(CollectionUtils.exists(list, null));
        assertFalse(CollectionUtils.exists(null, EQUALS_TWO));
        assertFalse(CollectionUtils.exists(list, EQUALS_TWO));
        list.add(1);
        list.add(3);
        list.add(4);
        assertFalse(CollectionUtils.exists(list, EQUALS_TWO));

        list.add(2);
        assertEquals(true, CollectionUtils.exists(list, EQUALS_TWO));
    }

// org.apache.commons.collections4.CollectionUtilsTest::select
    public void select() {
        final List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        
        final Collection<Integer> output1 = CollectionUtils.select(list, EQUALS_TWO);
        final Collection<Number> output2 = CollectionUtils.<Number>select(list, EQUALS_TWO);
        final HashSet<Number> output3 = CollectionUtils.select(list, EQUALS_TWO, new HashSet<Number>());
        assertTrue(CollectionUtils.isEqualCollection(output1, output3));
        assertEquals(4, list.size());
        assertEquals(1, output1.size());
        assertEquals(2, output2.iterator().next());
    }

// org.apache.commons.collections4.CollectionUtilsTest::selectRejected
    public void selectRejected() {
        final List<Long> list = new ArrayList<Long>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        list.add(4L);
        final Collection<Long> output1 = CollectionUtils.selectRejected(list, EQUALS_TWO);
        final Collection<? extends Number> output2 = CollectionUtils.selectRejected(list, EQUALS_TWO);
        final HashSet<Number> output3 = CollectionUtils.selectRejected(list, EQUALS_TWO, new HashSet<Number>());
        assertTrue(CollectionUtils.isEqualCollection(output1, output2));
        assertTrue(CollectionUtils.isEqualCollection(output1, output3));
        assertEquals(4, list.size());
        assertEquals(3, output1.size());
        assertTrue(output1.contains(1L));
        assertTrue(output1.contains(3L));
        assertTrue(output1.contains(4L));
    }

// org.apache.commons.collections4.CollectionUtilsTest::collect
    public void collect() {
        final Transformer<Number, Long> transformer = TransformerUtils.constantTransformer(2L);
        Collection<Number> collection = CollectionUtils.<Integer, Number>collect(iterableA, transformer);
        assertTrue(collection.size() == collectionA.size());
        assertCollectResult(collection);

        ArrayList<Number> list;
        list = CollectionUtils.collect(collectionA, transformer, new ArrayList<Number>());
        assertTrue(list.size() == collectionA.size());
        assertCollectResult(list);

        Iterator<Integer> iterator = null;
        list = CollectionUtils.collect(iterator, transformer, new ArrayList<Number>());

        iterator = iterableA.iterator();
        list = CollectionUtils.collect(iterator, transformer, list);
        assertTrue(collection.size() == collectionA.size());
        assertCollectResult(collection);

        iterator = collectionA.iterator();
        collection = CollectionUtils.<Integer, Number>collect(iterator, transformer);
        assertTrue(collection.size() == collectionA.size());
        assertTrue(collection.contains(2L) && !collection.contains(1));
        collection = CollectionUtils.collect((Iterator<Integer>) null, (Transformer<Integer, Number>) null);
        assertTrue(collection.size() == 0);

        final int size = collectionA.size();
        collectionB = CollectionUtils.collect((Collection<Integer>) null, transformer, collectionB);
        assertTrue(collectionA.size() == size && collectionA.contains(1));
        CollectionUtils.collect(collectionB, null, collectionA);
        assertTrue(collectionA.size() == size && collectionA.contains(1));

    }

// org.apache.commons.collections4.CollectionUtilsTest::transform1
    public void transform1() {
        List<Number> list = new ArrayList<Number>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        CollectionUtils.transform(list, TRANSFORM_TO_INTEGER);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));

        list = new ArrayList<Number>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        CollectionUtils.transform(null, TRANSFORM_TO_INTEGER);
        assertEquals(3, list.size());
        CollectionUtils.transform(list, null);
        assertEquals(3, list.size());
        CollectionUtils.transform(null, null);
        assertEquals(3, list.size());
    }

// org.apache.commons.collections4.CollectionUtilsTest::transform2
    public void transform2() {
        final Set<Number> set = new HashSet<Number>();
        set.add(1L);
        set.add(2L);
        set.add(3L);
        CollectionUtils.transform(set, new Transformer<Object, Integer>() {
            public Integer transform(final Object input) {
                return 4;
            }
        });
        assertEquals(1, set.size());
        assertEquals(4, set.iterator().next());
    }

// org.apache.commons.collections4.CollectionUtilsTest::addIgnoreNull
    public void addIgnoreNull() {
        final Set<String> set = new HashSet<String>();
        set.add("1");
        set.add("2");
        set.add("3");
        assertFalse(CollectionUtils.addIgnoreNull(set, null));
        assertEquals(3, set.size());
        assertFalse(CollectionUtils.addIgnoreNull(set, "1"));
        assertEquals(3, set.size());
        assertEquals(true, CollectionUtils.addIgnoreNull(set, "4"));
        assertEquals(4, set.size());
        assertEquals(true, set.contains("4"));
    }

// org.apache.commons.collections4.CollectionUtilsTest::predicatedCollection
    public void predicatedCollection() {
        final Predicate<Object> predicate = PredicateUtils.instanceofPredicate(Integer.class);
        Collection<Number> collection = CollectionUtils.predicatedCollection(new ArrayList<Number>(), predicate);
        assertTrue("returned object should be a PredicatedCollection", collection instanceof PredicatedCollection);
        try {
            CollectionUtils.predicatedCollection(new ArrayList<Number>(), null);
            fail("Expecting IllegalArgumentException for null predicate.");
        } catch (final IllegalArgumentException ex) {
            
        }
        try {
            CollectionUtils.predicatedCollection(null, predicate);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (final IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections4.CollectionUtilsTest::isFull
    public void isFull() {
        final Set<String> set = new HashSet<String>();
        set.add("1");
        set.add("2");
        set.add("3");
        try {
            CollectionUtils.isFull(null);
            fail();
        } catch (final NullPointerException ex) {
        }
        assertFalse(CollectionUtils.isFull(set));

        final CircularFifoQueue<String> buf = new CircularFifoQueue<String>(set);
        assertEquals(false, CollectionUtils.isFull(buf));
        buf.remove("2");
        assertFalse(CollectionUtils.isFull(buf));
        buf.add("2");
        assertEquals(false, CollectionUtils.isFull(buf));
    }

// org.apache.commons.collections4.CollectionUtilsTest::isEmpty
    public void isEmpty() {
        assertFalse(CollectionUtils.isNotEmpty(null));
        assertTrue(CollectionUtils.isNotEmpty(collectionA));
    }

// org.apache.commons.collections4.CollectionUtilsTest::maxSize
    public void maxSize() {
        final Set<String> set = new HashSet<String>();
        set.add("1");
        set.add("2");
        set.add("3");
        try {
            CollectionUtils.maxSize(null);
            fail();
        } catch (final NullPointerException ex) {
        }
        assertEquals(-1, CollectionUtils.maxSize(set));

        final Queue<String> buf = new CircularFifoQueue<String>(set);
        assertEquals(3, CollectionUtils.maxSize(buf));
        buf.remove("2");
        assertEquals(3, CollectionUtils.maxSize(buf));
        buf.add("2");
        assertEquals(3, CollectionUtils.maxSize(buf));
    }

// org.apache.commons.collections4.CollectionUtilsTest::intersectionUsesMethodEquals
    public void intersectionUsesMethodEquals() {
        
        final Integer elta = new Integer(17); 
        final Integer eltb = new Integer(17);

        
        assertEquals(elta, eltb);
        assertEquals(eltb, elta);

        
        assertTrue(elta != eltb);

        
        final Collection<Number> cola = new ArrayList<Number>();
        final Collection<Integer> colb = new ArrayList<Integer>();

        
        
        cola.add(elta);
        colb.add(eltb);

        
        
        final Collection<Number> intersection = CollectionUtils.intersection(cola, colb);
        assertEquals(1, intersection.size());

        
        
        
        final Object eltc = intersection.iterator().next();
        assertTrue(eltc == elta && eltc != eltb || eltc != elta && eltc == eltb);

        
        
        assertEquals(elta, eltc);
        assertEquals(eltc, elta);
        assertEquals(eltb, eltc);
        assertEquals(eltc, eltb);
    }

// org.apache.commons.collections4.CollectionUtilsTest::testRetainAll
    public void testRetainAll() {
        final List<String> base = new ArrayList<String>();
        base.add("A");
        base.add("B");
        base.add("C");
        final List<Object> sub = new ArrayList<Object>();
        sub.add("A");
        sub.add("C");
        sub.add("X");

        final Collection<String> result = CollectionUtils.retainAll(base, sub);
        assertEquals(2, result.size());
        assertEquals(true, result.contains("A"));
        assertFalse(result.contains("B"));
        assertEquals(true, result.contains("C"));
        assertEquals(3, base.size());
        assertEquals(true, base.contains("A"));
        assertEquals(true, base.contains("B"));
        assertEquals(true, base.contains("C"));
        assertEquals(3, sub.size());
        assertEquals(true, sub.contains("A"));
        assertEquals(true, sub.contains("C"));
        assertEquals(true, sub.contains("X"));

        try {
            CollectionUtils.retainAll(null, null);
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
        } 
    }

// org.apache.commons.collections4.CollectionUtilsTest::testRemoveAll
    public void testRemoveAll() {
        final List<String> base = new ArrayList<String>();
        base.add("A");
        base.add("B");
        base.add("C");
        final List<String> sub = new ArrayList<String>();
        sub.add("A");
        sub.add("C");
        sub.add("X");

        final Collection<String> result = CollectionUtils.removeAll(base, sub);
        assertEquals(1, result.size());
        assertFalse(result.contains("A"));
        assertEquals(true, result.contains("B"));
        assertFalse(result.contains("C"));
        assertEquals(3, base.size());
        assertEquals(true, base.contains("A"));
        assertEquals(true, base.contains("B"));
        assertEquals(true, base.contains("C"));
        assertEquals(3, sub.size());
        assertEquals(true, sub.contains("A"));
        assertEquals(true, sub.contains("C"));
        assertEquals(true, sub.contains("X"));

        try {
            CollectionUtils.removeAll(null, null);
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
        } 
    }

// org.apache.commons.collections4.CollectionUtilsTest::testTransformedCollection
    public void testTransformedCollection() {
        final Transformer<Object, Object> transformer = TransformerUtils.nopTransformer();
        Collection<Object> collection = CollectionUtils.transformingCollection(new ArrayList<Object>(), transformer);
        assertTrue("returned object should be a TransformedCollection", collection instanceof TransformedCollection);
        try {
            CollectionUtils.transformingCollection(new ArrayList<Object>(), null);
            fail("Expecting IllegalArgumentException for null transformer.");
        } catch (final IllegalArgumentException ex) {
            
        }
        try {
            CollectionUtils.transformingCollection(null, transformer);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (final IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections4.CollectionUtilsTest::testTransformedCollection_2
    public void testTransformedCollection_2() {
        final List<Object> list = new ArrayList<Object>();
        list.add("1");
        list.add("2");
        list.add("3");
        final Collection<Object> result = CollectionUtils.transformingCollection(list, TRANSFORM_TO_INTEGER);
        assertEquals(true, result.contains("1")); 
        assertEquals(true, result.contains("2")); 
        assertEquals(true, result.contains("3")); 
    }

// org.apache.commons.collections4.CollectionUtilsTest::testSynchronizedCollection
    public void testSynchronizedCollection() {
        Collection<Object> col = CollectionUtils.synchronizedCollection(new ArrayList<Object>());
        assertTrue("Returned object should be a SynchronizedCollection.", col instanceof SynchronizedCollection);
        try {
            CollectionUtils.synchronizedCollection(null);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (final IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections4.CollectionUtilsTest::testUnmodifiableCollection
    public void testUnmodifiableCollection() {
        Collection<Object> col = CollectionUtils.unmodifiableCollection(new ArrayList<Object>());
        assertTrue("Returned object should be a UnmodifiableCollection.", col instanceof UnmodifiableCollection);
        try {
            CollectionUtils.unmodifiableCollection(null);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (final IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections4.CollectionUtilsTest::emptyCollection
    public void emptyCollection() throws Exception {
        final Collection<Number> coll = CollectionUtils.emptyCollection();
        assertEquals(CollectionUtils.EMPTY_COLLECTION, coll);
    }

// org.apache.commons.collections4.CollectionUtilsTest::emptyIfNull
    public void emptyIfNull() {
        assertTrue(CollectionUtils.emptyIfNull(null).isEmpty());
        final Collection<Object> collection = new ArrayList<Object>();
        assertSame(collection, CollectionUtils.emptyIfNull(collection));
    }

// org.apache.commons.collections4.CollectionUtilsTest::addAllForIterable
    public void addAllForIterable() {
        final Collection<Integer> inputCollection = createMock(Collection.class);
        final Iterable<Integer> inputIterable = inputCollection;
        final Iterable<Long> iterable = createMock(Iterable.class);
        final Iterator<Long> iterator = createMock(Iterator.class);
        final Collection<Number> c = createMock(Collection.class);

        expect(iterable.iterator()).andReturn(iterator);
        next(iterator, 1L);
        next(iterator, 2L);
        next(iterator, 3L);
        expect(iterator.hasNext()).andReturn(false);
        expect(c.add(1L)).andReturn(true);
        expect(c.add(2L)).andReturn(true);
        expect(c.add(3L)).andReturn(true);
        
        
        expect(c.addAll(inputCollection)).andReturn(true);

        
        expect(iterable.iterator()).andReturn(iterator);
        next(iterator, 1L);
        expect(iterator.hasNext()).andReturn(false);
        expect(c.add(1L)).andReturn(false);
        expect(c.addAll(inputCollection)).andReturn(false);

        replay();
        assertTrue(CollectionUtils.addAll(c, iterable));
        assertTrue(CollectionUtils.addAll(c, inputIterable));

        assertFalse(CollectionUtils.addAll(c, iterable));
        assertFalse(CollectionUtils.addAll(c, inputIterable));
        verify();
    }

// org.apache.commons.collections4.CollectionUtilsTest::addAllForEnumeration
    public void addAllForEnumeration() {
        final Hashtable<Integer, Integer> h = new Hashtable<Integer, Integer>();
        h.put(5, 5);
        final Enumeration<? extends Integer> enumeration = h.keys();
        CollectionUtils.addAll(collectionA, enumeration);
        assertTrue(collectionA.contains(5));
    }

// org.apache.commons.collections4.CollectionUtilsTest::addAllForElements
    public void addAllForElements() {
        CollectionUtils.addAll(collectionA, new Integer[]{5});
        assertTrue(collectionA.contains(5));
    }

// org.apache.commons.collections4.CollectionUtilsTest::getNegative
    public void getNegative() {
        CollectionUtils.get((Object)collectionA, -3);
    }

// org.apache.commons.collections4.CollectionUtilsTest::getPositiveOutOfBounds
    public void getPositiveOutOfBounds() {
        CollectionUtils.get((Object)collectionA.iterator(), 30);
    }

// org.apache.commons.collections4.CollectionUtilsTest::get1
    public void get1() {
        CollectionUtils.get((Object)null, 0);
    }

// org.apache.commons.collections4.CollectionUtilsTest::get
    public void get() {
        assertEquals(2, CollectionUtils.get((Object)collectionA, 2));
        assertEquals(2, CollectionUtils.get((Object)collectionA.iterator(), 2));
        final Map<Integer, Integer> map = CollectionUtils.getCardinalityMap(collectionA);
        assertEquals(map.entrySet().iterator().next(), CollectionUtils.get(
                (Object)map, 0));
    }

// org.apache.commons.collections4.CollectionUtilsTest::reverse
    public void reverse() {
        CollectionUtils.reverseArray(new Object[] {});
        final Integer[] a = collectionA.toArray(new Integer[collectionA.size()]);
        CollectionUtils.reverseArray(a);
        
        Collections.reverse(collectionA);
        assertEquals(collectionA, Arrays.asList(a));
    }

// org.apache.commons.collections4.CollectionUtilsTest::extractSingleton
    public void extractSingleton() {
        ArrayList<String> coll = null;
        try {
            CollectionUtils.extractSingleton(coll);
            fail("expected IllegalArgumentException from extractSingleton(null)");
        } catch (final IllegalArgumentException e) {
        }
        coll = new ArrayList<String>();
        try {
            CollectionUtils.extractSingleton(coll);
            fail("expected IllegalArgumentException from extractSingleton(empty)");
        } catch (final IllegalArgumentException e) {
        }
        coll.add("foo");
        assertEquals("foo", CollectionUtils.extractSingleton(coll));
        coll.add("bar");
        try {
            CollectionUtils.extractSingleton(coll);
            fail("expected IllegalArgumentException from extractSingleton(size == 2)");
        } catch (final IllegalArgumentException e) {
        }
    }

// org.apache.commons.collections4.CollectionUtilsTest::collateException1
    public void collateException1() {
        CollectionUtils.collate(collectionA, null);
    }

// org.apache.commons.collections4.CollectionUtilsTest::collateException2
    public void collateException2() {
        CollectionUtils.collate(collectionA, collectionC, null);
    }

// org.apache.commons.collections4.CollectionUtilsTest::testCollate
    public void testCollate() {
        List<Integer> result = CollectionUtils.collate(emptyCollection, emptyCollection);
        assertEquals("Merge empty with empty", 0, result.size());

        result = CollectionUtils.collate(collectionA, emptyCollection);
        assertEquals("Merge empty with non-empty", collectionA, result);

        List<Integer> result1 = CollectionUtils.collate(collectionD, collectionE);
        List<Integer> result2 = CollectionUtils.collate(collectionE, collectionD);
        assertEquals("Merge two lists 1", result1, result2);

        List<Integer> combinedList = new ArrayList<Integer>();
        combinedList.addAll(collectionD);
        combinedList.addAll(collectionE);
        Collections.sort(combinedList);

        assertEquals("Merge two lists 2", combinedList, result2);

        final Comparator<Integer> reverseComparator =
                ComparatorUtils.reversedComparator(ComparatorUtils.<Integer>naturalComparator());

        result = CollectionUtils.collate(emptyCollection, emptyCollection, reverseComparator);
        assertEquals("Comparator Merge empty with empty", 0, result.size());

        Collections.reverse((List<Integer>) collectionD);
        Collections.reverse((List<Integer>) collectionE);
        Collections.reverse(combinedList);

        result1 = CollectionUtils.collate(collectionD, collectionE, reverseComparator);
        result2 = CollectionUtils.collate(collectionE, collectionD, reverseComparator);
        assertEquals("Comparator Merge two lists 1", result1, result2);
        assertEquals("Comparator Merge two lists 2", combinedList, result2);
    }

// org.apache.commons.collections4.CollectionUtilsTest::testCollateIgnoreDuplicates
    public void testCollateIgnoreDuplicates() {
        List<Integer> result1 = CollectionUtils.collate(collectionD, collectionE, false);
        List<Integer> result2 = CollectionUtils.collate(collectionE, collectionD, false);
        assertEquals("Merge two lists 1 - ignore duplicates", result1, result2);

        Set<Integer> combinedSet = new HashSet<Integer>();
        combinedSet.addAll(collectionD);
        combinedSet.addAll(collectionE);
        List<Integer> combinedList = new ArrayList<Integer>(combinedSet);
        Collections.sort(combinedList);

        assertEquals("Merge two lists 2 - ignore duplicates", combinedList, result2);
    }

// org.apache.commons.collections4.CollectionUtilsTest::testPermutationsWithNullCollection
    public void testPermutationsWithNullCollection() {
        CollectionUtils.permutations(null);
    }

// org.apache.commons.collections4.CollectionUtilsTest::testPermutations
    public void testPermutations() {
        List<Integer> sample = collectionA.subList(0, 5);
        Collection<List<Integer>> permutations = CollectionUtils.permutations(sample);

        
        int collSize = sample.size();
        int factorial = 1;
        for (int i = 1; i <= collSize; i++) {
            factorial *= i;
        }
        assertEquals(factorial, permutations.size());
    }

// org.apache.commons.collections4.CollectionUtilsTest::testMatchesAll
    public void testMatchesAll() {
        assertFalse(CollectionUtils.matchesAll(null, null));
        assertFalse(CollectionUtils.matchesAll(collectionA, null));

        Predicate<Integer> lessThanFive = new Predicate<Integer>() {
            public boolean evaluate(Integer object) {
                return object < 5;
            }
        };
        assertTrue(CollectionUtils.matchesAll(collectionA, lessThanFive));
        
        Predicate<Integer> lessThanFour = new Predicate<Integer>() {
            public boolean evaluate(Integer object) {
                return object < 4;
            }
        };
        assertFalse(CollectionUtils.matchesAll(collectionA, lessThanFour));
        
        assertTrue(CollectionUtils.matchesAll(null, lessThanFour));
        assertTrue(CollectionUtils.matchesAll(emptyCollection, lessThanFour));
    }

// org.apache.commons.collections4.collection.UnmodifiableBoundedCollectionTest::testUnmodifiable
    public void testUnmodifiable() {
        assertTrue(makeObject() instanceof Unmodifiable);
        assertTrue(makeFullCollection() instanceof Unmodifiable);
    }

// org.apache.commons.collections4.collection.UnmodifiableBoundedCollectionTest::testDecorateFactory
    public void testDecorateFactory() {
        final BoundedCollection<E> coll = makeFullCollection();
        assertSame(coll, UnmodifiableBoundedCollection.unmodifiableBoundedCollection(coll));

        try {
            UnmodifiableBoundedCollection.unmodifiableBoundedCollection(null);
            fail();
        } catch (final IllegalArgumentException ex) {}
    }
