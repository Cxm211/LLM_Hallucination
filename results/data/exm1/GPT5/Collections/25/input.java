// buggy code
    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E> iterator1,
                                                   final Iterator<? extends E> iterator2) {
        return new CollatingIterator<E>(comparator, iterator1, iterator2);
    }

    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Iterator<? extends E>... iterators) {
        return new CollatingIterator<E>(comparator, iterators);
    }

    public static <E> Iterator<E> collatedIterator(final Comparator<? super E> comparator,
                                                   final Collection<Iterator<? extends E>> iterators) {
        return new CollatingIterator<E>(comparator, iterators);
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

        Collection<String> strings = Arrays.asList("a", "b", "c");
        final StringBuffer result = new StringBuffer();
        result.append(CollectionUtils.forAllButLastDo(strings, new Closure<String>() {
            public void execute(String input) {
                result.append(input+";");
            }
        }));
        assertEquals("a;b;c", result.toString());

        Collection<String> oneString = Arrays.asList("a");
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

// org.apache.commons.collections4.CollectionUtilsTest::getFromPrimitiveArray
    public void getFromPrimitiveArray() throws Exception {
        
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

// org.apache.commons.collections4.CollectionUtilsTest::partition
    public void partition() {
        List<Integer> input = new ArrayList<Integer>();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add(4);
        List<List<Integer>> partitions = CollectionUtils.partition(input, EQUALS_TWO);
        assertEquals(2, partitions.size());
        
        
        Collection<Integer> partition = partitions.get(0);
        assertEquals(1, partition.size());
        assertEquals(2, CollectionUtils.extractSingleton(partition).intValue());
        
        
        Integer[] expected = {1, 3, 4};
        partition = partitions.get(1);
        Assert.assertArrayEquals(expected, partition.toArray());
        
        partitions = CollectionUtils.partition((List<Integer>) null, EQUALS_TWO);
        assertTrue(partitions.isEmpty());
        
        partitions = CollectionUtils.partition(input);
        assertEquals(1, partitions.size());
        assertEquals(input, partitions.get(0));
    }

// org.apache.commons.collections4.CollectionUtilsTest::partitionWithOutputCollections
    public void partitionWithOutputCollections() {
        List<Integer> input = new ArrayList<Integer>();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add(4);
        
        List<Integer> output = new ArrayList<Integer>();
        List<Integer> rejected = new ArrayList<Integer>();

        CollectionUtils.partition(input, EQUALS_TWO, output, rejected);

        
        assertEquals(1, output.size());
        assertEquals(2, CollectionUtils.extractSingleton(output).intValue());
        
        
        Integer[] expected = {1, 3, 4};
        Assert.assertArrayEquals(expected, rejected.toArray());
        
        output.clear();
        rejected.clear();
        CollectionUtils.partition((List<Integer>) null, EQUALS_TWO, output, rejected);
        assertTrue(output.isEmpty());
        assertTrue(rejected.isEmpty());
    }

// org.apache.commons.collections4.CollectionUtilsTest::partitionMultiplePredicates
    public void partitionMultiplePredicates() {
        List<Integer> input = new ArrayList<Integer>();
        input.add(1);
        input.add(2);
        input.add(3);
        input.add(4);
        @SuppressWarnings("unchecked")
        List<List<Integer>> partitions = CollectionUtils.partition(input, EQUALS_TWO, EVEN);

        
        Collection<Integer> partition = partitions.get(0);
        assertEquals(1, partition.size());
        assertEquals(2, partition.iterator().next().intValue());
        
        
        partition = partitions.get(1);
        assertEquals(1, partition.size());
        assertEquals(4, partition.iterator().next().intValue());
        
        
        Integer[] expected = {1, 3};
        partition = partitions.get(2);
        Assert.assertArrayEquals(expected, partition.toArray());
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
        assertEquals(map.entrySet().iterator().next(), CollectionUtils.get((Object)map, 0));
    }

// org.apache.commons.collections4.CollectionUtilsTest::getIterator
    public void getIterator() {
        final Iterator<Integer> it = collectionA.iterator();
        assertEquals(Integer.valueOf(2), CollectionUtils.get((Object) it, 2));
        assertTrue(it.hasNext());
        assertEquals(Integer.valueOf(4), CollectionUtils.get((Object) it, 6));
        assertFalse(it.hasNext());
    }

// org.apache.commons.collections4.CollectionUtilsTest::getEnumeration
    public void getEnumeration() {
        final Vector<Integer> vectorA = new Vector<Integer>(collectionA);
        final Enumeration<Integer> e = vectorA.elements();
        assertEquals(Integer.valueOf(2), CollectionUtils.get(e, 2));
        assertTrue(e.hasMoreElements());
        assertEquals(Integer.valueOf(4), CollectionUtils.get(e, 6));
        assertFalse(e.hasMoreElements());
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

// org.apache.commons.collections4.CollectionUtilsTest::testRemoveAllWithEquator
    public void testRemoveAllWithEquator() {
        final List<String> base = new ArrayList<String>();
        base.add("AC");
        base.add("BB");
        base.add("CA");

        final List<String> remove = new ArrayList<String>();
        remove.add("AA");
        remove.add("CX");
        remove.add("XZ");

        
        final Collection<String> result = CollectionUtils.removeAll(base, remove, new Equator<String>() {

            public boolean equate(String o1, String o2) {
                return o1.charAt(1) == o2.charAt(1);
            }

            public int hash(String o) {
                return o.charAt(1);
            }
        });

        assertEquals(2, result.size());
        assertTrue(result.contains("AC"));
        assertTrue(result.contains("BB"));
        assertFalse(result.contains("CA"));
        assertEquals(3, base.size());
        assertEquals(true, base.contains("AC"));
        assertEquals(true, base.contains("BB"));
        assertEquals(true, base.contains("CA"));
        assertEquals(3, remove.size());
        assertEquals(true, remove.contains("AA"));
        assertEquals(true, remove.contains("CX"));
        assertEquals(true, remove.contains("XZ"));

        try {
            CollectionUtils.removeAll(null, null, DefaultEquator.defaultEquator());
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
        } 

        try {
            CollectionUtils.removeAll(base, remove, null);
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
        } 
    }

// org.apache.commons.collections4.CollectionUtilsTest::testRetainAllWithEquator
    public void testRetainAllWithEquator() {
        final List<String> base = new ArrayList<String>();
        base.add("AC");
        base.add("BB");
        base.add("CA");

        final List<String> retain = new ArrayList<String>();
        retain.add("AA");
        retain.add("CX");
        retain.add("XZ");

        
        final Collection<String> result = CollectionUtils.retainAll(base, retain, new Equator<String>() {

            public boolean equate(String o1, String o2) {
                return o1.charAt(1) == o2.charAt(1);
            }

            public int hash(String o) {
                return o.charAt(1);
            }
        });
        assertEquals(1, result.size());
        assertTrue(result.contains("CA"));
        assertFalse(result.contains("BB"));
        assertFalse(result.contains("AC"));

        assertEquals(3, base.size());
        assertTrue(base.contains("AC"));
        assertTrue(base.contains("BB"));
        assertTrue(base.contains("CA"));

        assertEquals(3, retain.size());
        assertTrue(retain.contains("AA"));
        assertTrue(retain.contains("CX"));
        assertTrue(retain.contains("XZ"));

        try {
            CollectionUtils.retainAll(null, null, null);
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
        } 

        try {
            CollectionUtils.retainAll(base, retain, null);
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
        } 
    }

// org.apache.commons.collections4.EnumerationUtilsTest::testToListWithStringTokenizer
    public void testToListWithStringTokenizer() {
        final List<String> expectedList1 = new ArrayList<String>();
        final StringTokenizer st = new StringTokenizer(TO_LIST_FIXTURE);
             while (st.hasMoreTokens()) {
                 expectedList1.add(st.nextToken());
             }
        final List<String> expectedList2 = new ArrayList<String>();
        expectedList2.add("this");
        expectedList2.add("is");
        expectedList2.add("a");
        expectedList2.add("test");
        final List<String> actualList = EnumerationUtils.toList(new StringTokenizer(TO_LIST_FIXTURE));
        assertEquals(expectedList1, expectedList2);
        assertEquals(expectedList1, actualList);
        assertEquals(expectedList2, actualList);
    }

// org.apache.commons.collections4.EnumerationUtilsTest::testToListWithHashtable
    public void testToListWithHashtable() {
        final Hashtable<String, Integer> expected = new Hashtable<String, Integer>();
        expected.put("one", Integer.valueOf(1));
        expected.put("two", Integer.valueOf(2));
        expected.put("three", Integer.valueOf(3));
        
        final List<Integer> actualEltList = EnumerationUtils.toList(expected.elements());
        assertEquals(expected.size(), actualEltList.size());
        assertTrue(actualEltList.contains(Integer.valueOf(1)));
        assertTrue(actualEltList.contains(Integer.valueOf(2)));
        assertTrue(actualEltList.contains(Integer.valueOf(3)));
        final List<Integer> expectedEltList = new ArrayList<Integer>();
        expectedEltList.add(Integer.valueOf(1));
        expectedEltList.add(Integer.valueOf(2));
        expectedEltList.add(Integer.valueOf(3));
        assertTrue(actualEltList.containsAll(expectedEltList));

        
        final List<String> actualKeyList = EnumerationUtils.toList(expected.keys());
        assertEquals(expected.size(), actualEltList.size());
        assertTrue(actualKeyList.contains("one"));
        assertTrue(actualKeyList.contains("two"));
        assertTrue(actualKeyList.contains("three"));
        final List<String> expectedKeyList = new ArrayList<String>();
        expectedKeyList.add("one");
        expectedKeyList.add("two");
        expectedKeyList.add("three");
        assertTrue(actualKeyList.containsAll(expectedKeyList));
    }

// org.apache.commons.collections4.EnumerationUtilsTest::getFromEnumeration
    public void getFromEnumeration() throws Exception {
        
        final Vector<String> vector = new Vector<String>();
        vector.addElement("zero");
        vector.addElement("one");
        Enumeration<String> en = vector.elements();
        assertEquals("zero", EnumerationUtils.get(en, 0));
        en = vector.elements();
        assertEquals("one", EnumerationUtils.get(en, 1));

        
        try {
            EnumerationUtils.get(en, 3);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException e) {
            
        }
        assertTrue(!en.hasMoreElements());
    }

// org.apache.commons.collections4.IterableUtilsTest::apply
    public void apply() {
        final List<Integer> listA = new ArrayList<Integer>();
        listA.add(1);

        final List<Integer> listB = new ArrayList<Integer>();
        listB.add(2);

        final Closure<List<Integer>> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<List<Integer>> col = new ArrayList<List<Integer>>();
        col.add(listA);
        col.add(listB);
        IterableUtils.apply(col, testClosure);
        assertTrue(listA.isEmpty() && listB.isEmpty());
        try {
            IterableUtils.apply(col, null);
            fail("expecting NullPointerException");
        } catch (NullPointerException npe) {
            
        }

        IterableUtils.apply(null, testClosure);

        
        col.add(null);
        IterableUtils.apply(col, testClosure);
    }

// org.apache.commons.collections4.IterableUtilsTest::applyFailure
    public void applyFailure() {
        final Closure<String> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<String> col = new ArrayList<String>();
        col.add("x");
        IterableUtils.apply(col, testClosure);
    }

// org.apache.commons.collections4.IterableUtilsTest::containsWithEquator
    public void containsWithEquator() {
        final List<String> base = new ArrayList<String>();
        base.add("AC");
        base.add("BB");
        base.add("CA");

        final Equator<String> secondLetterEquator = new Equator<String>() {

            public boolean equate(String o1, String o2) {
                return o1.charAt(1) == o2.charAt(1);
            }

            public int hash(String o) {
                return o.charAt(1);
            }

        };

        assertFalse(base.contains("CC"));
        assertTrue(IterableUtils.contains(base, "AC", secondLetterEquator));
        assertTrue(IterableUtils.contains(base, "CC", secondLetterEquator));
        assertFalse(IterableUtils.contains(base, "CX", secondLetterEquator));
        assertFalse(IterableUtils.contains(null, null, secondLetterEquator));

        try {
            IterableUtils.contains(base, "AC", null);
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
        } 
    }

// org.apache.commons.collections4.IterableUtilsTest::find
    public void find() {
        Predicate<Number> testPredicate = equalPredicate((Number) 4);
        Integer test = IterableUtils.find(iterableA, testPredicate);
        assertTrue(test.equals(4));
        testPredicate = equalPredicate((Number) 45);
        test = IterableUtils.find(iterableA, testPredicate);
        assertTrue(test == null);
        assertNull(IterableUtils.find(null,testPredicate));
        try {
            assertNull(IterableUtils.find(iterableA, null));
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
            
        }
    }

// org.apache.commons.collections4.IterableUtilsTest::frequency
    public void frequency() {
        assertEquals(4, IterableUtils.frequency(iterableB, EQUALS_TWO));
        assertEquals(0, IterableUtils.frequency(null, EQUALS_TWO));

        try {
            assertEquals(0, IterableUtils.frequency(iterableA, null));
            fail("predicate must not be null");
        } catch (NullPointerException ex) {
            
        }

        try {
            assertEquals(0, IterableUtils.frequency(null, null));
            fail("predicate must not be null");
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.collections4.IterableUtilsTest::matchesAny
    public void matchesAny() {
        final List<Integer> list = new ArrayList<Integer>();
        
        try {
            assertFalse(IterableUtils.matchesAny(null, null));
            fail("predicate must not be null");
        } catch (NullPointerException ex) {
            
        }

        try {
            assertFalse(IterableUtils.matchesAny(list, null));
            fail("predicate must not be null");
        } catch (NullPointerException ex) {
            
        }

        assertFalse(IterableUtils.matchesAny(null, EQUALS_TWO));
        assertFalse(IterableUtils.matchesAny(list, EQUALS_TWO));
        list.add(1);
        list.add(3);
        list.add(4);
        assertFalse(IterableUtils.matchesAny(list, EQUALS_TWO));

        list.add(2);
        assertEquals(true, IterableUtils.matchesAny(list, EQUALS_TWO));
    }

// org.apache.commons.collections4.IterableUtilsTest::matchesAll
    public void matchesAll() {
        try {
            assertFalse(IterableUtils.matchesAll(null, null));
            fail("predicate must not be null");
        } catch (NullPointerException ex) {
            
        }

        try {
            assertFalse(IterableUtils.matchesAll(iterableA, null));
            fail("predicate must not be null");
        } catch (NullPointerException ex) {
            
        }

        Predicate<Integer> lessThanFive = new Predicate<Integer>() {
            public boolean evaluate(Integer object) {
                return object < 5;
            }
        };
        assertTrue(IterableUtils.matchesAll(iterableA, lessThanFive));

        Predicate<Integer> lessThanFour = new Predicate<Integer>() {
            public boolean evaluate(Integer object) {
                return object < 4;
            }
        };
        assertFalse(IterableUtils.matchesAll(iterableA, lessThanFour));

        assertTrue(IterableUtils.matchesAll(null, lessThanFour));
        assertTrue(IterableUtils.matchesAll(emptyIterable, lessThanFour));
    }

// org.apache.commons.collections4.IterableUtilsTest::getFromIterable
    public void getFromIterable() throws Exception {
        
        final Bag<String> bag = new HashBag<String>();
        bag.add("element", 1);
        assertEquals("element", IterableUtils.get(bag, 0));

        
        IterableUtils.get(bag, 1);
    }

// org.apache.commons.collections4.IterableUtilsTest::testToString
    public void testToString() {
        String result = IterableUtils.toString(iterableA);
        assertEquals("[1, 2, 2, 3, 3, 3, 4, 4, 4, 4]", result);
        
        result = IterableUtils.toString(new ArrayList<Integer>());
        assertEquals("[]", result);

        result = IterableUtils.toString(null);
        assertEquals("[]", result);

        result = IterableUtils.toString(iterableA, new Transformer<Integer, String>() {
            public String transform(Integer input) {
                return new Integer(input * 2).toString();
            }
        });
        assertEquals("[2, 4, 4, 6, 6, 6, 8, 8, 8, 8]", result);

        result = IterableUtils.toString(new ArrayList<Integer>(), new Transformer<Integer, String>() {
            public String transform(Integer input) {
                fail("not supposed to reach here");
                return "";
            }
        });
        assertEquals("[]", result);

        result = IterableUtils.toString(null, new Transformer<Integer, String>() {
            public String transform(Integer input) {
                fail("not supposed to reach here");
                return "";
            }
        });
        assertEquals("[]", result);
    }

// org.apache.commons.collections4.IterableUtilsTest::testToStringDelimiter
    public void testToStringDelimiter() {
        
        Transformer<Integer, String> transformer = new Transformer<Integer, String>() {
            public String transform(Integer input) {
                return new Integer(input * 2).toString();
            }
        };
        
        String result = IterableUtils.toString(iterableA, transformer, "", "", "");
        assertEquals("2446668888", result);
        
        result = IterableUtils.toString(iterableA, transformer, ",", "", "");
        assertEquals("2,4,4,6,6,6,8,8,8,8", result);
        
        result = IterableUtils.toString(iterableA, transformer, "", "[", "]");
        assertEquals("[2446668888]", result);
        
        result = IterableUtils.toString(iterableA, transformer, ",", "[", "]");
        assertEquals("[2,4,4,6,6,6,8,8,8,8]", result);
        
        result = IterableUtils.toString(iterableA, transformer, ",", "[[", "]]");
        assertEquals("[[2,4,4,6,6,6,8,8,8,8]]", result);
        
        result = IterableUtils.toString(iterableA, transformer, ",,", "[", "]");
        assertEquals("[2,,4,,4,,6,,6,,6,,8,,8,,8,,8]", result);
        
        result = IterableUtils.toString(iterableA, transformer, ",,", "((", "))");
        assertEquals("((2,,4,,4,,6,,6,,6,,8,,8,,8,,8))", result);

        result = IterableUtils.toString(new ArrayList<Integer>(), transformer, "", "(", ")");
        assertEquals("()", result);
        
        result = IterableUtils.toString(new ArrayList<Integer>(), transformer, "", "", "");
        assertEquals("", result);
    }

// org.apache.commons.collections4.IterableUtilsTest::testToStringWithNullArguments
    public void testToStringWithNullArguments() {
        String result = IterableUtils.toString(null, new Transformer<Integer, String>() {
            public String transform(Integer input) {
                fail("not supposed to reach here");
                return "";
            }
        }, "", "(", ")");
        assertEquals("()", result);

        try {
            IterableUtils.toString(new ArrayList<Integer>(), null, "", "(", ")");
            fail("expecting NullPointerException");
        } catch (final NullPointerException ex) {
            
        }

        try {
            IterableUtils.toString(new ArrayList<Integer>(), new Transformer<Integer, String>() {
                public String transform(Integer input) {
                    fail("not supposed to reach here");
                    return "";
                }
            }, null, "(", ")");
            fail("expecting NullPointerException");
        } catch (final NullPointerException ex) {
            
        }

        try {
            IterableUtils.toString(new ArrayList<Integer>(), new Transformer<Integer, String>() {
                public String transform(Integer input) {
                    fail("not supposed to reach here");
                    return "";
                }
            }, "", null, ")");
            fail("expecting NullPointerException");
        } catch (final NullPointerException ex) {
            
        }

        try {
            IterableUtils.toString(new ArrayList<Integer>(), new Transformer<Integer, String>() {
                public String transform(Integer input) {
                    fail("not supposed to reach here");
                    return "";
                }
            }, "", "(", null);
            fail("expecting NullPointerException");
        } catch (final NullPointerException ex) {
            
        }
    }

// org.apache.commons.collections4.IteratorUtilsTest::testAsIterable
    public void testAsIterable() {
        final List<Integer> list = new ArrayList<Integer>();
        list.add(Integer.valueOf(0));
        list.add(Integer.valueOf(1));
        list.add(Integer.valueOf(2));
        final Iterator<Integer> iterator = list.iterator();

        final Iterable<Integer> iterable = IteratorUtils.asIterable(iterator);
        int expected = 0;
        for(final Integer actual : iterable) {
            assertEquals(expected, actual.intValue());
            ++expected;
        }
        
        assertTrue(expected > 0);

        
        assertFalse("should not be able to iterate twice", IteratorUtils.asIterable(iterator).iterator().hasNext());
    }

// org.apache.commons.collections4.IteratorUtilsTest::testAsIterableNull
    public void testAsIterableNull() {
        try {
            IteratorUtils.asIterable(null);
            fail("Expecting NullPointerException");
        } catch (final NullPointerException ex) {
            
        }
    }

// org.apache.commons.collections4.IteratorUtilsTest::testAsMultipleIterable
    public void testAsMultipleIterable() {
        final List<Integer> list = new ArrayList<Integer>();
        list.add(Integer.valueOf(0));
        list.add(Integer.valueOf(1));
        list.add(Integer.valueOf(2));
        final Iterator<Integer> iterator = list.iterator();

        final Iterable<Integer> iterable = IteratorUtils.asMultipleUseIterable(iterator);
        int expected = 0;
        for(final Integer actual : iterable) {
            assertEquals(expected, actual.intValue());
            ++expected;
        }
        
        assertTrue(expected > 0);

        
        expected = 0;
        for(final Integer actual : iterable) {
            assertEquals(expected, actual.intValue());
            ++expected;
        }
        
        assertTrue(expected > 0);
    }

// org.apache.commons.collections4.IteratorUtilsTest::testAsMultipleIterableNull
    public void testAsMultipleIterableNull() {
        try {
            IteratorUtils.asMultipleUseIterable(null);
            fail("Expecting NullPointerException");
        } catch (final NullPointerException ex) {
            
        }
    }

// org.apache.commons.collections4.IteratorUtilsTest::testToList
    public void testToList() {
        final List<Object> list = new ArrayList<Object>();
        list.add(Integer.valueOf(1));
        list.add("Two");
        list.add(null);
        final List<Object> result = IteratorUtils.toList(list.iterator());
        assertEquals(list, result);
    }

// org.apache.commons.collections4.IteratorUtilsTest::testToArray
    public void testToArray() {
        final List<Object> list = new ArrayList<Object>();
        list.add(Integer.valueOf(1));
        list.add("Two");
        list.add(null);
        final Object[] result = IteratorUtils.toArray(list.iterator());
        assertEquals(list, Arrays.asList(result));
    }

// org.apache.commons.collections4.IteratorUtilsTest::testToArray2
    public void testToArray2() {
        final List<String> list = new ArrayList<String>();
        list.add("One");
        list.add("Two");
        list.add(null);
        final String[] result = IteratorUtils.toArray(list.iterator(), String.class);
        assertEquals(list, Arrays.asList(result));
    }

// org.apache.commons.collections4.IteratorUtilsTest::testArrayIterator
    public void testArrayIterator() {
        final Object[] objArray = {"a", "b", "c"};
        ResettableIterator<Object> iterator = IteratorUtils.arrayIterator(objArray);
        assertTrue(iterator.next().equals("a"));
        assertTrue(iterator.next().equals("b"));
        iterator.reset();
        assertTrue(iterator.next().equals("a"));

        try {
            iterator = IteratorUtils.arrayIterator(Integer.valueOf(0));
            fail("Expecting IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
                
        }

        try {
            iterator = IteratorUtils.arrayIterator((Object[]) null);
            fail("Expecting NullPointerException");
        } catch (final NullPointerException ex) {
                
        }

        iterator = IteratorUtils.arrayIterator(objArray, 1);
        assertTrue(iterator.next().equals("b"));

        try {
            iterator = IteratorUtils.arrayIterator(objArray, -1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayIterator(objArray, 3);
        assertTrue(!iterator.hasNext());
        iterator.reset();

        try {
            iterator = IteratorUtils.arrayIterator(objArray, 4);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayIterator(objArray, 2, 3);
        assertTrue(iterator.next().equals("c"));

        try {
            iterator = IteratorUtils.arrayIterator(objArray, 2, 4);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayIterator(objArray, -1, 1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayIterator(objArray, 2, 1);
            fail("Expecting IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            
        }

        final int[] intArray = {0, 1, 2};
        iterator = IteratorUtils.arrayIterator(intArray);
        assertTrue(iterator.next().equals(Integer.valueOf(0)));
        assertTrue(iterator.next().equals(Integer.valueOf(1)));
        iterator.reset();
        assertTrue(iterator.next().equals(Integer.valueOf(0)));

        iterator = IteratorUtils.arrayIterator(intArray, 1);
        assertTrue(iterator.next().equals(Integer.valueOf(1)));

        try {
            iterator = IteratorUtils.arrayIterator(intArray, -1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayIterator(intArray, 3);
        assertTrue(!iterator.hasNext());
        iterator.reset();

        try {
            iterator = IteratorUtils.arrayIterator(intArray, 4);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayIterator(intArray, 2, 3);
        assertTrue(iterator.next().equals(Integer.valueOf(2)));

        try {
            iterator = IteratorUtils.arrayIterator(intArray, 2, 4);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayIterator(intArray, -1, 1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayIterator(intArray, 2, 1);
            fail("Expecting IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections4.IteratorUtilsTest::testArrayListIterator
    public void testArrayListIterator() {
        final Object[] objArray = {"a", "b", "c", "d"};
        ResettableListIterator<Object> iterator = IteratorUtils.arrayListIterator(objArray);
        assertTrue(!iterator.hasPrevious());
        assertTrue(iterator.previousIndex() == -1);
        assertTrue(iterator.nextIndex() == 0);
        assertTrue(iterator.next().equals("a"));
        assertTrue(iterator.previous().equals("a"));
        assertTrue(iterator.next().equals("a"));
        assertTrue(iterator.previousIndex() == 0);
        assertTrue(iterator.nextIndex() == 1);
        assertTrue(iterator.next().equals("b"));
        assertTrue(iterator.next().equals("c"));
        assertTrue(iterator.next().equals("d"));
        assertTrue(iterator.nextIndex() == 4); 
        assertTrue(iterator.previousIndex() == 3);

        try {
            iterator = IteratorUtils.arrayListIterator(Integer.valueOf(0));
            fail("Expecting IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
                
        }

        try {
            iterator = IteratorUtils.arrayListIterator((Object[]) null);
            fail("Expecting NullPointerException");
        } catch (final NullPointerException ex) {
                
        }

        iterator = IteratorUtils.arrayListIterator(objArray, 1);
        assertTrue(iterator.previousIndex() == -1);
        assertTrue(!iterator.hasPrevious());
        assertTrue(iterator.nextIndex() == 0);
        assertTrue(iterator.next().equals("b"));
        assertTrue(iterator.previousIndex() == 0);

        try {
            iterator = IteratorUtils.arrayListIterator(objArray, -1);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayListIterator(objArray, 3);
        assertTrue(iterator.hasNext());
        try {
            iterator.previous();
            fail("Expecting NoSuchElementException.");
        } catch (final NoSuchElementException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayListIterator(objArray, 5);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayListIterator(objArray, 2, 3);
        assertTrue(iterator.next().equals("c"));

        try {
            iterator = IteratorUtils.arrayListIterator(objArray, 2, 5);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayListIterator(objArray, -1, 1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayListIterator(objArray, 2, 1);
            fail("Expecting IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            
        }

        final int[] intArray = {0, 1, 2};
        iterator = IteratorUtils.arrayListIterator(intArray);
        assertTrue(iterator.previousIndex() == -1);
        assertTrue(!iterator.hasPrevious());
        assertTrue(iterator.nextIndex() == 0);
        assertTrue(iterator.next().equals(Integer.valueOf(0)));
        assertTrue(iterator.previousIndex() == 0);
        assertTrue(iterator.nextIndex() == 1);
        assertTrue(iterator.next().equals(Integer.valueOf(1)));
        assertTrue(iterator.previousIndex() == 1);
        assertTrue(iterator.nextIndex() == 2);
        assertTrue(iterator.previous().equals(Integer.valueOf(1)));
        assertTrue(iterator.next().equals(Integer.valueOf(1)));

        iterator = IteratorUtils.arrayListIterator(intArray, 1);
        assertTrue(iterator.previousIndex() == -1);
        assertTrue(!iterator.hasPrevious());
        assertTrue(iterator.nextIndex() == 0);
        assertTrue(iterator.next().equals(Integer.valueOf(1)));
        assertTrue(iterator.previous().equals(Integer.valueOf(1)));
        assertTrue(iterator.next().equals(Integer.valueOf(1)));
        assertTrue(iterator.previousIndex() == 0);
        assertTrue(iterator.nextIndex() == 1);
        assertTrue(iterator.next().equals(Integer.valueOf(2)));
        assertTrue(iterator.previousIndex() == 1);
        assertTrue(iterator.nextIndex() == 2);
        assertTrue(iterator.previous().equals(Integer.valueOf(2)));
        assertTrue(iterator.previousIndex() == 0);
        assertTrue(iterator.nextIndex() == 1);

        try {
            iterator = IteratorUtils.arrayListIterator(intArray, -1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayListIterator(intArray, 3);
        assertTrue(!iterator.hasNext());

        try {
            iterator = IteratorUtils.arrayListIterator(intArray, 4);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        iterator = IteratorUtils.arrayListIterator(intArray, 2, 3);
        assertTrue(!iterator.hasPrevious());
        assertTrue(iterator.previousIndex() == -1);
        assertTrue(iterator.next().equals(Integer.valueOf(2)));
        assertTrue(iterator.hasPrevious());
        assertTrue(!iterator.hasNext());

        try {
            iterator = IteratorUtils.arrayListIterator(intArray, 2, 4);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayListIterator(intArray, -1, 1);
            fail("Expecting IndexOutOfBoundsException");
        } catch (final IndexOutOfBoundsException ex) {
            
        }

        try {
            iterator = IteratorUtils.arrayListIterator(intArray, 2, 1);
            fail("Expecting IllegalArgumentException");
        } catch (final IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections4.IteratorUtilsTest::testEmptyIterator
    public void testEmptyIterator() {
        assertSame(EmptyIterator.INSTANCE, IteratorUtils.EMPTY_ITERATOR);
        assertSame(EmptyIterator.RESETTABLE_INSTANCE, IteratorUtils.EMPTY_ITERATOR);
        assertEquals(true, IteratorUtils.EMPTY_ITERATOR instanceof Iterator);
        assertEquals(true, IteratorUtils.EMPTY_ITERATOR instanceof ResettableIterator);
        assertEquals(false, IteratorUtils.EMPTY_ITERATOR instanceof OrderedIterator);
        assertEquals(false, IteratorUtils.EMPTY_ITERATOR instanceof ListIterator);
        assertEquals(false, IteratorUtils.EMPTY_ITERATOR instanceof MapIterator);
        assertEquals(false, IteratorUtils.EMPTY_ITERATOR.hasNext());
        IteratorUtils.EMPTY_ITERATOR.reset();
        assertSame(IteratorUtils.EMPTY_ITERATOR, IteratorUtils.EMPTY_ITERATOR);
        assertSame(IteratorUtils.EMPTY_ITERATOR, IteratorUtils.emptyIterator());
        try {
            IteratorUtils.EMPTY_ITERATOR.next();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_ITERATOR.remove();
            fail();
        } catch (final IllegalStateException ex) {}
    }

// org.apache.commons.collections4.IteratorUtilsTest::testEmptyListIterator
    public void testEmptyListIterator() {
        assertSame(EmptyListIterator.INSTANCE, IteratorUtils.EMPTY_LIST_ITERATOR);
        assertSame(EmptyListIterator.RESETTABLE_INSTANCE, IteratorUtils.EMPTY_LIST_ITERATOR);
        assertEquals(true, IteratorUtils.EMPTY_LIST_ITERATOR instanceof Iterator);
        assertEquals(true, IteratorUtils.EMPTY_LIST_ITERATOR instanceof ListIterator);
        assertEquals(true, IteratorUtils.EMPTY_LIST_ITERATOR instanceof ResettableIterator);
        assertEquals(true, IteratorUtils.EMPTY_LIST_ITERATOR instanceof ResettableListIterator);
        assertEquals(false, IteratorUtils.EMPTY_LIST_ITERATOR instanceof MapIterator);
        assertEquals(false, IteratorUtils.EMPTY_LIST_ITERATOR.hasNext());
        assertEquals(0, IteratorUtils.EMPTY_LIST_ITERATOR.nextIndex());
        assertEquals(-1, IteratorUtils.EMPTY_LIST_ITERATOR.previousIndex());
        IteratorUtils.EMPTY_LIST_ITERATOR.reset();
        assertSame(IteratorUtils.EMPTY_LIST_ITERATOR, IteratorUtils.EMPTY_LIST_ITERATOR);
        assertSame(IteratorUtils.EMPTY_LIST_ITERATOR, IteratorUtils.emptyListIterator());
        try {
            IteratorUtils.EMPTY_LIST_ITERATOR.next();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_LIST_ITERATOR.previous();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_LIST_ITERATOR.remove();
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.emptyListIterator().set(null);
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.emptyListIterator().add(null);
            fail();
        } catch (final UnsupportedOperationException ex) {}
    }

// org.apache.commons.collections4.IteratorUtilsTest::testEmptyMapIterator
    public void testEmptyMapIterator() {
        assertSame(EmptyMapIterator.INSTANCE, IteratorUtils.EMPTY_MAP_ITERATOR);
        assertEquals(true, IteratorUtils.EMPTY_MAP_ITERATOR instanceof Iterator);
        assertEquals(true, IteratorUtils.EMPTY_MAP_ITERATOR instanceof MapIterator);
        assertEquals(true, IteratorUtils.EMPTY_MAP_ITERATOR instanceof ResettableIterator);
        assertEquals(false, IteratorUtils.EMPTY_MAP_ITERATOR instanceof ListIterator);
        assertEquals(false, IteratorUtils.EMPTY_MAP_ITERATOR instanceof OrderedIterator);
        assertEquals(false, IteratorUtils.EMPTY_MAP_ITERATOR instanceof OrderedMapIterator);
        assertEquals(false, IteratorUtils.EMPTY_MAP_ITERATOR.hasNext());
        ((ResettableIterator<Object>) IteratorUtils.EMPTY_MAP_ITERATOR).reset();
        assertSame(IteratorUtils.EMPTY_MAP_ITERATOR, IteratorUtils.EMPTY_MAP_ITERATOR);
        assertSame(IteratorUtils.EMPTY_MAP_ITERATOR, IteratorUtils.emptyMapIterator());
        try {
            IteratorUtils.EMPTY_MAP_ITERATOR.next();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_MAP_ITERATOR.remove();
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.EMPTY_MAP_ITERATOR.getKey();
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.EMPTY_MAP_ITERATOR.getValue();
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.EMPTY_MAP_ITERATOR.setValue(null);
            fail();
        } catch (final IllegalStateException ex) {}
    }

// org.apache.commons.collections4.IteratorUtilsTest::testEmptyOrderedIterator
    public void testEmptyOrderedIterator() {
        assertSame(EmptyOrderedIterator.INSTANCE, IteratorUtils.EMPTY_ORDERED_ITERATOR);
        assertEquals(true, IteratorUtils.EMPTY_ORDERED_ITERATOR instanceof Iterator);
        assertEquals(true, IteratorUtils.EMPTY_ORDERED_ITERATOR instanceof OrderedIterator);
        assertEquals(true, IteratorUtils.EMPTY_ORDERED_ITERATOR instanceof ResettableIterator);
        assertEquals(false, IteratorUtils.EMPTY_ORDERED_ITERATOR instanceof ListIterator);
        assertEquals(false, IteratorUtils.EMPTY_ORDERED_ITERATOR instanceof MapIterator);
        assertEquals(false, IteratorUtils.EMPTY_ORDERED_ITERATOR.hasNext());
        assertEquals(false, IteratorUtils.EMPTY_ORDERED_ITERATOR.hasPrevious());
        ((ResettableIterator<Object>) IteratorUtils.EMPTY_ORDERED_ITERATOR).reset();
        assertSame(IteratorUtils.EMPTY_ORDERED_ITERATOR, IteratorUtils.EMPTY_ORDERED_ITERATOR);
        assertSame(IteratorUtils.EMPTY_ORDERED_ITERATOR, IteratorUtils.emptyOrderedIterator());
        try {
            IteratorUtils.EMPTY_ORDERED_ITERATOR.next();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_ORDERED_ITERATOR.previous();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_ORDERED_ITERATOR.remove();
            fail();
        } catch (final IllegalStateException ex) {}
    }

// org.apache.commons.collections4.IteratorUtilsTest::testEmptyOrderedMapIterator
    public void testEmptyOrderedMapIterator() {
        assertSame(EmptyOrderedMapIterator.INSTANCE, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR);
        assertEquals(true, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR instanceof Iterator);
        assertEquals(true, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR instanceof MapIterator);
        assertEquals(true, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR instanceof OrderedMapIterator);
        assertEquals(true, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR instanceof ResettableIterator);
        assertEquals(false, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR instanceof ListIterator);
        assertEquals(false, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.hasNext());
        assertEquals(false, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.hasPrevious());
        ((ResettableIterator<Object>) IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR).reset();
        assertSame(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR, IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR);
        assertSame(IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR, IteratorUtils.emptyOrderedMapIterator());
        try {
            IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.next();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.previous();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.remove();
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.getKey();
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.getValue();
            fail();
        } catch (final IllegalStateException ex) {}
        try {
            IteratorUtils.EMPTY_ORDERED_MAP_ITERATOR.setValue(null);
            fail();
        } catch (final IllegalStateException ex) {}
    }

// org.apache.commons.collections4.IteratorUtilsTest::testUnmodifiableIteratorIteration
    public void testUnmodifiableIteratorIteration() {
        final Iterator<String> iterator = getImmutableIterator();

        assertTrue(iterator.hasNext());

        assertEquals("a", iterator.next());

        assertTrue(iterator.hasNext());

        assertEquals("b", iterator.next());

        assertTrue(iterator.hasNext());

        assertEquals("c", iterator.next());

        assertTrue(iterator.hasNext());

        assertEquals("d", iterator.next());

        assertTrue(!iterator.hasNext());
    }

// org.apache.commons.collections4.IteratorUtilsTest::testUnmodifiableListIteratorIteration
    public void testUnmodifiableListIteratorIteration() {
        final ListIterator<String> listIterator = getImmutableListIterator();

        assertTrue(!listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());

        assertEquals("a", listIterator.next());

        assertTrue(listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());

        assertEquals("b", listIterator.next());

        assertTrue(listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());

        assertEquals("c", listIterator.next());

        assertTrue(listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());

        assertEquals("d", listIterator.next());

        assertTrue(listIterator.hasPrevious());
        assertTrue(!listIterator.hasNext());

        assertEquals("d", listIterator.previous());

        assertTrue(listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());

        assertEquals("c", listIterator.previous());

        assertTrue(listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());

        assertEquals("b", listIterator.previous());

        assertTrue(listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());

        assertEquals("a", listIterator.previous());

        assertTrue(!listIterator.hasPrevious());
        assertTrue(listIterator.hasNext());
    }

// org.apache.commons.collections4.IteratorUtilsTest::testUnmodifiableIteratorImmutability
    public void testUnmodifiableIteratorImmutability() {
        final Iterator<String> iterator = getImmutableIterator();

        try {
            iterator.remove();
            
            fail("remove() should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }

        iterator.next();

        try {
            iterator.remove();
            
            fail("remove() should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }

    }

// org.apache.commons.collections4.IteratorUtilsTest::testUnmodifiableListIteratorImmutability
    public void testUnmodifiableListIteratorImmutability() {
        final ListIterator<String> listIterator = getImmutableListIterator();

        try {
            listIterator.remove();
            
            fail("remove() should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }

        try {
            listIterator.set("a");
            
            fail("set(Object) should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }

        try {
            listIterator.add("a");
            
            fail("add(Object) should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }

        listIterator.next();

        try {
            listIterator.remove();
            
            fail("remove() should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }

        try {
            listIterator.set("a");
            
            fail("set(Object) should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }

        try {
            listIterator.add("a");
            
            fail("add(Object) should throw an UnsupportedOperationException");
        } catch (final UnsupportedOperationException e) {
            
        }
    }

// org.apache.commons.collections4.IteratorUtilsTest::testNodeListIterator
    public void testNodeListIterator() {
        final Node[] nodes = createNodes();
        final NodeList nodeList = createNodeList(nodes);

        final Iterator<Node> iterator = IteratorUtils.nodeListIterator(nodeList);
        int expectedNodeIndex = 0;
        for (final Node actual : IteratorUtils.asIterable(iterator)) {
            assertEquals(nodes[expectedNodeIndex], actual);
            ++expectedNodeIndex;
        }

        
        assertTrue(expectedNodeIndex > 0);

        
        assertFalse("should not be able to iterate twice", IteratorUtils.asIterable(iterator).iterator().hasNext());
    }

// org.apache.commons.collections4.IteratorUtilsTest::testNodeIterator
    public void testNodeIterator() {
        final Node[] nodes = createNodes();
        final NodeList nodeList = createNodeList(nodes);
        final Node parentNode = createMock(Node.class);
        expect(parentNode.getChildNodes()).andStubReturn(nodeList);
        replay(parentNode);

        final Iterator<Node> iterator = IteratorUtils.nodeListIterator(parentNode);
        int expectedNodeIndex = 0;
        for (final Node actual : IteratorUtils.asIterable(iterator)) {
            assertEquals(nodes[expectedNodeIndex], actual);
            ++expectedNodeIndex;
        }

        
        assertTrue(expectedNodeIndex > 0);

        
        assertFalse("should not be able to iterate twice", IteratorUtils.asIterable(iterator).iterator().hasNext());
    }

// org.apache.commons.collections4.IteratorUtilsTest::testCollatedIterator
    public void testCollatedIterator() {
        try {
            IteratorUtils.collatedIterator(null, collectionOdd.iterator(), null);
            fail("expecting NullPointerException");
        } catch (NullPointerException npe) {
            
        }

        try {
            IteratorUtils.collatedIterator(null, null, collectionEven.iterator());
            fail("expecting NullPointerException");
        } catch (NullPointerException npe) {
            
        }

        
        Iterator<Integer> it = 
                IteratorUtils.collatedIterator(null, collectionOdd.iterator(), collectionEven.iterator());

        List<Integer> result = IteratorUtils.toList(it);
        assertEquals(12, result.size());

        List<Integer> combinedList = new ArrayList<Integer>();
        combinedList.addAll(collectionOdd);
        combinedList.addAll(collectionEven);
        Collections.sort(combinedList);

        assertEquals(combinedList, result);

        it = IteratorUtils.collatedIterator(null, collectionOdd.iterator(), emptyCollection.iterator());
        result = IteratorUtils.toList(it);
        assertEquals(collectionOdd, result);

        final Comparator<Integer> reverseComparator =
                ComparatorUtils.reversedComparator(ComparatorUtils.<Integer>naturalComparator());

        Collections.reverse((List<Integer>) collectionOdd);
        Collections.reverse((List<Integer>) collectionEven);
        Collections.reverse(combinedList);

        it = IteratorUtils.collatedIterator(reverseComparator,
                                            collectionOdd.iterator(),
                                            collectionEven.iterator());
        result = IteratorUtils.toList(it);
        assertEquals(combinedList, result);
    }

// org.apache.commons.collections4.IteratorUtilsTest::apply
    public void apply() {
        final List<Integer> listA = new ArrayList<Integer>();
        listA.add(1);

        final List<Integer> listB = new ArrayList<Integer>();
        listB.add(2);

        final Closure<List<Integer>> testClosure = ClosureUtils.invokerClosure("clear");
        final Collection<List<Integer>> col = new ArrayList<List<Integer>>();
        col.add(listA);
        col.add(listB);
        IteratorUtils.apply(col.iterator(), testClosure);
        assertTrue(listA.isEmpty() && listB.isEmpty());
        try {
            IteratorUtils.apply(col.iterator(), null);
            fail("expecting NullPointerException");
        } catch (NullPointerException npe) {
            
        }

        IteratorUtils.apply(null, testClosure);

        
        col.add(null);
        IteratorUtils.apply(col.iterator(), testClosure);
    }

// org.apache.commons.collections4.IteratorUtilsTest::find
    public void find() {
        Predicate<Number> testPredicate = equalPredicate((Number) 4);
        Integer test = IteratorUtils.find(iterableA.iterator(), testPredicate);
        assertTrue(test.equals(4));
        testPredicate = equalPredicate((Number) 45);
        test = IteratorUtils.find(iterableA.iterator(), testPredicate);
        assertTrue(test == null);
        assertNull(IteratorUtils.find(null,testPredicate));
        try {
            assertNull(IteratorUtils.find(iterableA.iterator(), null));
            fail("expecting NullPointerException");
        } catch (final NullPointerException npe) {
            
        }
    }

// org.apache.commons.collections4.IteratorUtilsTest::getFromIterator
    public void getFromIterator() throws Exception {
        
        Iterator<Integer> iterator = iterableA.iterator();
        assertEquals(1, (int) IteratorUtils.get(iterator, 0));
        iterator = iterableA.iterator();
        assertEquals(2, (int) IteratorUtils.get(iterator, 1));

        
        try {
            IteratorUtils.get(iterator, 10);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (final IndexOutOfBoundsException e) {
            
        }
        assertTrue(!iterator.hasNext());
    }

// org.apache.commons.collections4.iterators.IteratorChainTest::testIterator
    public void testIterator() {
        final Iterator<String> iter = makeObject();
        for (final String testValue : testArray) {
            final Object iterValue = iter.next();

            assertEquals( "Iteration value is correct", testValue, iterValue );
        }

        assertTrue("Iterator should now be empty", !iter.hasNext());

        try {
            iter.next();
        } catch (final Exception e) {
            assertTrue("NoSuchElementException must be thrown",
                       e.getClass().equals(new NoSuchElementException().getClass()));
        }
    }

// org.apache.commons.collections4.iterators.IteratorChainTest::testRemoveFromFilteredIterator
    public void testRemoveFromFilteredIterator() {

        final Predicate<Integer> myPredicate = new Predicate<Integer>() {
            public boolean evaluate(final Integer i) {
                return i.compareTo(Integer.valueOf(4)) < 0;
            }
        };

        final List<Integer> list1 = new ArrayList<Integer>();
        final List<Integer> list2 = new ArrayList<Integer>();

        list1.add(Integer.valueOf(1));
        list1.add(Integer.valueOf(2));
        list2.add(Integer.valueOf(3));
        list2.add(Integer.valueOf(4)); 

        final Iterator<Integer> it1 = IteratorUtils.filteredIterator(list1.iterator(), myPredicate);
        final Iterator<Integer> it2 = IteratorUtils.filteredIterator(list2.iterator(), myPredicate);

        final Iterator<Integer> it = IteratorUtils.chainedIterator(it1, it2);
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertEquals(0, list1.size());
        assertEquals(1, list2.size());
    }

// org.apache.commons.collections4.iterators.IteratorChainTest::testRemove
    public void testRemove() {
        final Iterator<String> iter = makeObject();

        try {
            iter.remove();
            fail("Calling remove before the first call to next() should throw an exception");
        } catch (final IllegalStateException e) {

        }

        for (final String testValue : testArray) {
            final String iterValue = iter.next();

            assertEquals("Iteration value is correct", testValue, iterValue);

            if (!iterValue.equals("Four")) {
                iter.remove();
            }
        }

        assertTrue("List is empty",list1.size() == 0);
        assertTrue("List is empty",list2.size() == 1);
        assertTrue("List is empty",list3.size() == 0);
    }

// org.apache.commons.collections4.iterators.IteratorChainTest::testFirstIteratorIsEmptyBug
    public void testFirstIteratorIsEmptyBug() {
        final List<String> empty = new ArrayList<String>();
        final List<String> notEmpty = new ArrayList<String>();
        notEmpty.add("A");
        notEmpty.add("B");
        notEmpty.add("C");
        final IteratorChain<String> chain = new IteratorChain<String>();
        chain.addIterator(empty.iterator());
        chain.addIterator(notEmpty.iterator());
        assertTrue("should have next",chain.hasNext());
        assertEquals("A",chain.next());
        assertTrue("should have next",chain.hasNext());
        assertEquals("B",chain.next());
        assertTrue("should have next",chain.hasNext());
        assertEquals("C",chain.next());
        assertTrue("should not have next",!chain.hasNext());
    }

// org.apache.commons.collections4.iterators.IteratorChainTest::testEmptyChain
    public void testEmptyChain() {
        final IteratorChain<Object> chain = new IteratorChain<Object>();
        assertEquals(false, chain.hasNext());
        try {
            chain.next();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            chain.remove();
            fail();
        } catch (final IllegalStateException ex) {}
    }

// org.apache.commons.collections4.iterators.LazyIteratorChainTest::testIterator
    public void testIterator() {
        final Iterator<String> iter = makeObject();
        for (final String testValue : testArray) {
            final Object iterValue = iter.next();

            assertEquals( "Iteration value is correct", testValue, iterValue );
        }

        assertTrue("Iterator should now be empty", !iter.hasNext());

        try {
            iter.next();
        } catch (final Exception e) {
            assertTrue("NoSuchElementException must be thrown",
                       e.getClass().equals(new NoSuchElementException().getClass()));
        }
    }

// org.apache.commons.collections4.iterators.LazyIteratorChainTest::testRemoveFromFilteredIterator
    public void testRemoveFromFilteredIterator() {

        final Predicate<Integer> myPredicate = new Predicate<Integer>() {
            public boolean evaluate(final Integer i) {
                return i.compareTo(Integer.valueOf(4)) < 0;
            }
        };

        final List<Integer> list1 = new ArrayList<Integer>();
        final List<Integer> list2 = new ArrayList<Integer>();

        list1.add(Integer.valueOf(1));
        list1.add(Integer.valueOf(2));
        list2.add(Integer.valueOf(3));
        list2.add(Integer.valueOf(4)); 

        final Iterator<Integer> it1 = IteratorUtils.filteredIterator(list1.iterator(), myPredicate);
        final Iterator<Integer> it2 = IteratorUtils.filteredIterator(list2.iterator(), myPredicate);

        final Iterator<Integer> it = IteratorUtils.chainedIterator(it1, it2);
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertEquals(0, list1.size());
        assertEquals(1, list2.size());
    }

// org.apache.commons.collections4.iterators.LazyIteratorChainTest::testRemove
    public void testRemove() {
        final Iterator<String> iter = makeObject();

        try {
            iter.remove();
            fail("Calling remove before the first call to next() should throw an exception");
        } catch (final IllegalStateException e) {

        }

        for (final String testValue : testArray) {
            final String iterValue = iter.next();

            assertEquals("Iteration value is correct", testValue, iterValue);

            if (!iterValue.equals("Four")) {
                iter.remove();
            }
        }

        assertTrue("List is empty",list1.size() == 0);
        assertTrue("List is empty",list2.size() == 1);
        assertTrue("List is empty",list3.size() == 0);
    }

// org.apache.commons.collections4.iterators.LazyIteratorChainTest::testFirstIteratorIsEmptyBug
    public void testFirstIteratorIsEmptyBug() {
        final List<String> empty = new ArrayList<String>();
        final List<String> notEmpty = new ArrayList<String>();
        notEmpty.add("A");
        notEmpty.add("B");
        notEmpty.add("C");
        final LazyIteratorChain<String> chain = new LazyIteratorChain<String>() {
            @Override
            protected Iterator<String> nextIterator(final int count) {
                switch (count) {
                case 1:
                    return empty.iterator();
                case 2:
                    return notEmpty.iterator();
                }
                return null;
            }
        };
        assertTrue("should have next",chain.hasNext());
        assertEquals("A",chain.next());
        assertTrue("should have next",chain.hasNext());
        assertEquals("B",chain.next());
        assertTrue("should have next",chain.hasNext());
        assertEquals("C",chain.next());
        assertTrue("should not have next",!chain.hasNext());
    }

// org.apache.commons.collections4.iterators.LazyIteratorChainTest::testEmptyChain
    public void testEmptyChain() {
        final LazyIteratorChain<String> chain = makeEmptyIterator();
        assertEquals(false, chain.hasNext());
        try {
            chain.next();
            fail();
        } catch (final NoSuchElementException ex) {}
        try {
            chain.remove();
            fail();
        } catch (final IllegalStateException ex) {}
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructor_null1
    public void testIteratorConstructor_null1() {
        final Iterator<Object> it = new ObjectGraphIterator<Object>(null);

        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
        try {
            it.remove();
            fail();
        } catch (final IllegalStateException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructor_null_next
    public void testIteratorConstructor_null_next() {
        final Iterator<Object> it = new ObjectGraphIterator<Object>(null);
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructor_null_remove
    public void testIteratorConstructor_null_remove() {
        final Iterator<Object> it = new ObjectGraphIterator<Object>(null);
        try {
            it.remove();
            fail();
        } catch (final IllegalStateException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructorIteration_Empty
    public void testIteratorConstructorIteration_Empty() {
        final List<Iterator<Object>> iteratorList = new ArrayList<Iterator<Object>>();
        final Iterator<Object> it = new ObjectGraphIterator<Object>(iteratorList.iterator());

        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
        try {
            it.remove();
            fail();
        } catch (final IllegalStateException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructorIteration_Simple
    public void testIteratorConstructorIteration_Simple() {
        final List<Iterator<String>> iteratorList = new ArrayList<Iterator<String>>();
        iteratorList.add(list1.iterator());
        iteratorList.add(list2.iterator());
        iteratorList.add(list3.iterator());
        final Iterator<Object> it = new ObjectGraphIterator<Object>(iteratorList.iterator());

        for (int i = 0; i < 6; i++) {
            assertEquals(true, it.hasNext());
            assertEquals(testArray[i], it.next());
        }
        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructorIteration_SimpleNoHasNext
    public void testIteratorConstructorIteration_SimpleNoHasNext() {
        final List<Iterator<String>> iteratorList = new ArrayList<Iterator<String>>();
        iteratorList.add(list1.iterator());
        iteratorList.add(list2.iterator());
        iteratorList.add(list3.iterator());
        final Iterator<Object> it = new ObjectGraphIterator<Object>(iteratorList.iterator());

        for (int i = 0; i < 6; i++) {
            assertEquals(testArray[i], it.next());
        }
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructorIteration_WithEmptyIterators
    public void testIteratorConstructorIteration_WithEmptyIterators() {
        final List<Iterator<String>> iteratorList = new ArrayList<Iterator<String>>();
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        iteratorList.add(list1.iterator());
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        iteratorList.add(list2.iterator());
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        iteratorList.add(list3.iterator());
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        final Iterator<Object> it = new ObjectGraphIterator<Object>(iteratorList.iterator());

        for (int i = 0; i < 6; i++) {
            assertEquals(true, it.hasNext());
            assertEquals(testArray[i], it.next());
        }
        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteratorConstructorRemove
    public void testIteratorConstructorRemove() {
        final List<Iterator<String>> iteratorList = new ArrayList<Iterator<String>>();
        iteratorList.add(list1.iterator());
        iteratorList.add(list2.iterator());
        iteratorList.add(list3.iterator());
        final Iterator<Object> it = new ObjectGraphIterator<Object>(iteratorList.iterator());

        for (int i = 0; i < 6; i++) {
            assertEquals(testArray[i], it.next());
            it.remove();
        }
        assertEquals(false, it.hasNext());
        assertEquals(0, list1.size());
        assertEquals(0, list2.size());
        assertEquals(0, list3.size());
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteration_IteratorOfIterators
    public void testIteration_IteratorOfIterators() {
        final List<Iterator<String>> iteratorList = new ArrayList<Iterator<String>>();
        iteratorList.add(list1.iterator());
        iteratorList.add(list2.iterator());
        iteratorList.add(list3.iterator());
        final Iterator<Object> it = new ObjectGraphIterator<Object>(iteratorList.iterator(), null);

        for (int i = 0; i < 6; i++) {
            assertEquals(true, it.hasNext());
            assertEquals(testArray[i], it.next());
        }
        assertEquals(false, it.hasNext());
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteration_IteratorOfIteratorsWithEmptyIterators
    public void testIteration_IteratorOfIteratorsWithEmptyIterators() {
        final List<Iterator<String>> iteratorList = new ArrayList<Iterator<String>>();
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        iteratorList.add(list1.iterator());
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        iteratorList.add(list2.iterator());
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        iteratorList.add(list3.iterator());
        iteratorList.add(IteratorUtils.<String>emptyIterator());
        final Iterator<Object> it = new ObjectGraphIterator<Object>(iteratorList.iterator(), null);

        for (int i = 0; i < 6; i++) {
            assertEquals(true, it.hasNext());
            assertEquals(testArray[i], it.next());
        }
        assertEquals(false, it.hasNext());
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteration_RootNull
    public void testIteration_RootNull() {
        final Iterator<Object> it = new ObjectGraphIterator<Object>(null, null);

        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
        try {
            it.remove();
            fail();
        } catch (final IllegalStateException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteration_RootNoTransformer
    public void testIteration_RootNoTransformer() {
        final Forest forest = new Forest();
        final Iterator<Object> it = new ObjectGraphIterator<Object>(forest, null);

        assertEquals(true, it.hasNext());
        assertSame(forest, it.next());
        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteration_Transformed1
    public void testIteration_Transformed1() {
        final Forest forest = new Forest();
        final Leaf l1 = forest.addTree().addBranch().addLeaf();
        final Iterator<Object> it = new ObjectGraphIterator<Object>(forest, new LeafFinder());

        assertEquals(true, it.hasNext());
        assertSame(l1, it.next());
        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteration_Transformed2
    public void testIteration_Transformed2() {
        final Forest forest = new Forest();
        forest.addTree();
        forest.addTree();
        forest.addTree();
        final Branch b1 = forest.getTree(0).addBranch();
        final Branch b2 = forest.getTree(0).addBranch();
        final Branch b3 = forest.getTree(2).addBranch();
         forest.getTree(2).addBranch();
        final Branch b5 = forest.getTree(2).addBranch();
        final Leaf l1 = b1.addLeaf();
        final Leaf l2 = b1.addLeaf();
        final Leaf l3 = b2.addLeaf();
        final Leaf l4 = b3.addLeaf();
        final Leaf l5 = b5.addLeaf();

        final Iterator<Object> it = new ObjectGraphIterator<Object>(forest, new LeafFinder());

        assertEquals(true, it.hasNext());
        assertSame(l1, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l2, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l3, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l4, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l5, it.next());
        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ObjectGraphIteratorTest::testIteration_Transformed3
    public void testIteration_Transformed3() {
        final Forest forest = new Forest();
        forest.addTree();
        forest.addTree();
        forest.addTree();
        final Branch b1 = forest.getTree(1).addBranch();
        final Branch b2 = forest.getTree(1).addBranch();
        final Branch b3 = forest.getTree(2).addBranch();
        final Branch b4 = forest.getTree(2).addBranch();
         forest.getTree(2).addBranch();
        final Leaf l1 = b1.addLeaf();
        final Leaf l2 = b1.addLeaf();
        final Leaf l3 = b2.addLeaf();
        final Leaf l4 = b3.addLeaf();
        final Leaf l5 = b4.addLeaf();

        final Iterator<Object> it = new ObjectGraphIterator<Object>(forest, new LeafFinder());

        assertEquals(true, it.hasNext());
        assertSame(l1, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l2, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l3, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l4, it.next());
        assertEquals(true, it.hasNext());
        assertSame(l5, it.next());
        assertEquals(false, it.hasNext());
        try {
            it.next();
            fail();
        } catch (final NoSuchElementException ex) {
        }
    }

// org.apache.commons.collections4.iterators.ZippingIteratorTest::testIterateEven
    public void testIterateEven() {
        @SuppressWarnings("unchecked")
        final ZippingIterator<Integer> iter = new ZippingIterator<Integer>(evens.iterator());
        for (int i = 0; i < evens.size(); i++) {
            assertTrue(iter.hasNext());
            assertEquals(evens.get(i), iter.next());
        }
        assertTrue(!iter.hasNext());
    }

// org.apache.commons.collections4.iterators.ZippingIteratorTest::testIterateEvenOdd
    public void testIterateEvenOdd() {
        final ZippingIterator<Integer> iter = new ZippingIterator<Integer>(evens.iterator(), odds.iterator());
        for (int i = 0; i < 20; i++) {
            assertTrue(iter.hasNext());
            assertEquals(Integer.valueOf(i), iter.next());
        }
        assertTrue(!iter.hasNext());
    }

// org.apache.commons.collections4.iterators.ZippingIteratorTest::testIterateOddEven
    public void testIterateOddEven() {
        final ZippingIterator<Integer> iter = new ZippingIterator<Integer>(odds.iterator(), evens.iterator());
        for (int i = 0, j = 0; i < 20; i++) {
            assertTrue(iter.hasNext());
            int val = iter.next();
            if (i % 2 == 0) {
                assertEquals(odds.get(j).intValue(), val);
            } else {
                assertEquals(evens.get(j).intValue(), val);
                j++;
            }
        }
        assertTrue(!iter.hasNext());
    }

// org.apache.commons.collections4.iterators.ZippingIteratorTest::testIterateEvenEven
    public void testIterateEvenEven() {
        final ZippingIterator<Integer> iter = new ZippingIterator<Integer>(evens.iterator(), evens.iterator());
        for (int i = 0; i < evens.size(); i++) {
            assertTrue(iter.hasNext());
            assertEquals(evens.get(i), iter.next());
            assertTrue(iter.hasNext());
            assertEquals(evens.get(i), iter.next());
        }
        assertTrue(!iter.hasNext());
    }

// org.apache.commons.collections4.iterators.ZippingIteratorTest::testIterateFibEvenOdd
    public void testIterateFibEvenOdd() {
        final ZippingIterator<Integer> iter = new ZippingIterator<Integer>(fib.iterator(), evens.iterator(), odds.iterator());

        assertEquals(Integer.valueOf(1),iter.next());  
        assertEquals(Integer.valueOf(0),iter.next());  
        assertEquals(Integer.valueOf(1),iter.next());  
        assertEquals(Integer.valueOf(1),iter.next());  
        assertEquals(Integer.valueOf(2),iter.next());  
        assertEquals(Integer.valueOf(3),iter.next());  
        assertEquals(Integer.valueOf(2),iter.next());  
        assertEquals(Integer.valueOf(4),iter.next());  
        assertEquals(Integer.valueOf(5),iter.next());  
        assertEquals(Integer.valueOf(3),iter.next());  
        assertEquals(Integer.valueOf(6),iter.next());  
        assertEquals(Integer.valueOf(7),iter.next());  
        assertEquals(Integer.valueOf(5),iter.next());  
        assertEquals(Integer.valueOf(8),iter.next());  
        assertEquals(Integer.valueOf(9),iter.next());  
        assertEquals(Integer.valueOf(8),iter.next());  
        assertEquals(Integer.valueOf(10),iter.next()); 
        assertEquals(Integer.valueOf(11),iter.next()); 
        assertEquals(Integer.valueOf(13),iter.next()); 
        assertEquals(Integer.valueOf(12),iter.next()); 
        assertEquals(Integer.valueOf(13),iter.next()); 
        assertEquals(Integer.valueOf(21),iter.next()); 
        assertEquals(Integer.valueOf(14),iter.next()); 
        assertEquals(Integer.valueOf(15),iter.next()); 
        assertEquals(Integer.valueOf(16),iter.next()); 
        assertEquals(Integer.valueOf(17),iter.next()); 
        assertEquals(Integer.valueOf(18),iter.next()); 
        assertEquals(Integer.valueOf(19),iter.next()); 

        assertTrue(!iter.hasNext());
    }

// org.apache.commons.collections4.iterators.ZippingIteratorTest::testRemoveFromSingle
    public void testRemoveFromSingle() {
        @SuppressWarnings("unchecked")
        final ZippingIterator<Integer> iter = new ZippingIterator<Integer>(evens.iterator());
        int expectedSize = evens.size();
        while (iter.hasNext()) {
            final Object o = iter.next();
            final Integer val = (Integer) o;
            if (val.intValue() % 4 == 0) {
                expectedSize--;
                iter.remove();
            }
        }
        assertEquals(expectedSize, evens.size());
    }

// org.apache.commons.collections4.iterators.ZippingIteratorTest::testRemoveFromDouble
    public void testRemoveFromDouble() {
        final ZippingIterator<Integer> iter = new ZippingIterator<Integer>(evens.iterator(), odds.iterator());
        int expectedSize = evens.size() + odds.size();
        while (iter.hasNext()) {
            final Object o = iter.next();
            final Integer val = (Integer) o;
            if (val.intValue() % 4 == 0 || val.intValue() % 3 == 0) {
                expectedSize--;
                iter.remove();
            }
        }
        assertEquals(expectedSize, evens.size() + odds.size());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testNoMappingReturnsNull
    public void testNoMappingReturnsNull() {
        final MultiValueMap<K, V> map = createTestMap();
        assertNull(map.get("whatever"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testValueCollectionType
    public void testValueCollectionType() {
        final MultiValueMap<K, V> map = createTestMap(LinkedList.class);
        assertTrue(map.get("one") instanceof LinkedList);
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testMultipleValues
    public void testMultipleValues() {
        final MultiValueMap<K, V> map = createTestMap(HashSet.class);
        final HashSet<V> expected = new HashSet<V>();
        expected.add((V) "uno");
        expected.add((V) "un");
        assertEquals(expected, map.get("one"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testContainsValue
    public void testContainsValue() {
        final MultiValueMap<K, V> map = createTestMap(HashSet.class);
        assertTrue(map.containsValue("uno"));
        assertTrue(map.containsValue("un"));
        assertTrue(map.containsValue("dos"));
        assertTrue(map.containsValue("deux"));
        assertTrue(map.containsValue("tres"));
        assertTrue(map.containsValue("trois"));
        assertFalse(map.containsValue("quatro"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testKeyContainsValue
    public void testKeyContainsValue() {
        final MultiValueMap<K, V> map = createTestMap(HashSet.class);
        assertTrue(map.containsValue("one", "uno"));
        assertTrue(map.containsValue("one", "un"));
        assertTrue(map.containsValue("two", "dos"));
        assertTrue(map.containsValue("two", "deux"));
        assertTrue(map.containsValue("three", "tres"));
        assertTrue(map.containsValue("three", "trois"));
        assertFalse(map.containsValue("four", "quatro"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testValues
    public void testValues() {
        final MultiValueMap<K, V> map = createTestMap(HashSet.class);
        final HashSet<V> expected = new HashSet<V>();
        expected.add((V) "uno");
        expected.add((V) "dos");
        expected.add((V) "tres");
        expected.add((V) "un");
        expected.add((V) "deux");
        expected.add((V) "trois");
        final Collection<Object> c = map.values();
        assertEquals(6, c.size());
        assertEquals(expected, new HashSet<Object>(c));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testKeyedIterator
    public void testKeyedIterator() {
        final MultiValueMap<K, V> map = createTestMap();
        final ArrayList<Object> actual = new ArrayList<Object>(IteratorUtils.toList(map.iterator("one")));
        final ArrayList<Object> expected = new ArrayList<Object>(Arrays.asList("uno", "un"));
        assertEquals(expected, actual);
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testRemoveAllViaIterator
    public void testRemoveAllViaIterator() {
        final MultiValueMap<K, V> map = createTestMap();
        for (final Iterator<?> i = map.values().iterator(); i.hasNext();) {
            i.next();
            i.remove();
        }
        assertNull(map.get("one"));
        assertTrue(map.isEmpty());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testRemoveAllViaKeyedIterator
    public void testRemoveAllViaKeyedIterator() {
        final MultiValueMap<K, V> map = createTestMap();
        for (final Iterator<?> i = map.iterator("one"); i.hasNext();) {
            i.next();
            i.remove();
        }
        assertNull(map.get("one"));
        assertEquals(4, map.totalSize());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testIterator
    public void testIterator() {
        final MultiValueMap<K, V> map = createTestMap();
        @SuppressWarnings("unchecked")
        Collection<V> values = new ArrayList<V>((Collection<V>) map.values());
        Iterator<Map.Entry<K, V>> iterator = map.iterator();
        while (iterator.hasNext()) {
            Map.Entry<K, V> entry = iterator.next();
            assertTrue(map.containsValue(entry.getKey(), entry.getValue()));
            assertTrue(values.contains(entry.getValue()));
            assertTrue(values.remove(entry.getValue()));
        }
        assertTrue(values.isEmpty());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testRemoveAllViaEntryIterator
    public void testRemoveAllViaEntryIterator() {
        final MultiValueMap<K, V> map = createTestMap();
        for (final Iterator<?> i = map.iterator(); i.hasNext();) {
            i.next();
            i.remove();
        }
        assertNull(map.get("one"));
        assertEquals(0, map.totalSize());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testTotalSizeA
    public void testTotalSizeA() {
        assertEquals(6, createTestMap().totalSize());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testMapEquals
    public void testMapEquals() {
        final MultiValueMap<K, V> one = new MultiValueMap<K, V>();
        final Integer value = Integer.valueOf(1);
        one.put((K) "One", value);
        one.removeMapping("One", value);

        final MultiValueMap<K, V> two = new MultiValueMap<K, V>();
        assertEquals(two, one);
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testGetCollection
    public void testGetCollection() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        map.put((K) "A", "AA");
        assertSame(map.get("A"), map.getCollection("A"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testTotalSize
    public void testTotalSize() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        assertEquals(0, map.totalSize());
        map.put((K) "A", "AA");
        assertEquals(1, map.totalSize());
        map.put((K) "B", "BA");
        assertEquals(2, map.totalSize());
        map.put((K) "B", "BB");
        assertEquals(3, map.totalSize());
        map.put((K) "B", "BC");
        assertEquals(4, map.totalSize());
        map.remove("A");
        assertEquals(3, map.totalSize());
        map.removeMapping("B", "BC");
        assertEquals(2, map.totalSize());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testSize
    public void testSize() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        assertEquals(0, map.size());
        map.put((K) "A", "AA");
        assertEquals(1, map.size());
        map.put((K) "B", "BA");
        assertEquals(2, map.size());
        map.put((K) "B", "BB");
        assertEquals(2, map.size());
        map.put((K) "B", "BC");
        assertEquals(2, map.size());
        map.remove("A");
        assertEquals(1, map.size());
        map.removeMapping("B", "BC");
        assertEquals(1, map.size());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testSize_Key
    public void testSize_Key() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        assertEquals(0, map.size("A"));
        assertEquals(0, map.size("B"));
        map.put((K) "A", "AA");
        assertEquals(1, map.size("A"));
        assertEquals(0, map.size("B"));
        map.put((K) "B", "BA");
        assertEquals(1, map.size("A"));
        assertEquals(1, map.size("B"));
        map.put((K) "B", "BB");
        assertEquals(1, map.size("A"));
        assertEquals(2, map.size("B"));
        map.put((K) "B", "BC");
        assertEquals(1, map.size("A"));
        assertEquals(3, map.size("B"));
        map.remove("A");
        assertEquals(0, map.size("A"));
        assertEquals(3, map.size("B"));
        map.removeMapping("B", "BC");
        assertEquals(0, map.size("A"));
        assertEquals(2, map.size("B"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testIterator_Key
    public void testIterator_Key() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        assertEquals(false, map.iterator("A").hasNext());
        map.put((K) "A", "AA");
        final Iterator<?> it = map.iterator("A");
        assertEquals(true, it.hasNext());
        it.next();
        assertEquals(false, it.hasNext());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testContainsValue_Key
    public void testContainsValue_Key() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        assertEquals(false, map.containsValue("A", "AA"));
        assertEquals(false, map.containsValue("B", "BB"));
        map.put((K) "A", "AA");
        assertEquals(true, map.containsValue("A", "AA"));
        assertEquals(false, map.containsValue("A", "AB"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testPutWithList
    public void testPutWithList() {
        @SuppressWarnings("rawtypes")
        final MultiValueMap<K, V> test = MultiValueMap.multiValueMap(new HashMap<K, Collection>(), ArrayList.class);
        assertEquals("a", test.put((K) "A", "a"));
        assertEquals("b", test.put((K) "A", "b"));
        assertEquals(1, test.size());
        assertEquals(2, test.size("A"));
        assertEquals(2, test.totalSize());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testPutWithSet
    public void testPutWithSet() {
        @SuppressWarnings("rawtypes")
        final MultiValueMap<K, V> test = MultiValueMap.multiValueMap(new HashMap<K, HashSet>(), HashSet.class);
        assertEquals("a", test.put((K) "A", "a"));
        assertEquals("b", test.put((K) "A", "b"));
        assertEquals(null, test.put((K) "A", "a"));
        assertEquals(1, test.size());
        assertEquals(2, test.size("A"));
        assertEquals(2, test.totalSize());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testPutAll_Map1
    public void testPutAll_Map1() {
        final MultiMap<K, V> original = new MultiValueMap<K, V>();
        original.put((K) "key", "object1");
        original.put((K) "key", "object2");

        final MultiValueMap<K, V> test = new MultiValueMap<K, V>();
        test.put((K) "keyA", "objectA");
        test.put((K) "key", "object0");
        test.putAll(original);

        assertEquals(2, test.size());
        assertEquals(4, test.totalSize());
        assertEquals(1, test.getCollection("keyA").size());
        assertEquals(3, test.getCollection("key").size());
        assertEquals(true, test.containsValue("objectA"));
        assertEquals(true, test.containsValue("object0"));
        assertEquals(true, test.containsValue("object1"));
        assertEquals(true, test.containsValue("object2"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testPutAll_Map2
    public void testPutAll_Map2() {
        final Map<K, V> original = new HashMap<K, V>();
        original.put((K) "keyX", (V) "object1");
        original.put((K) "keyY", (V) "object2");

        final MultiValueMap<K, V> test = new MultiValueMap<K, V>();
        test.put((K) "keyA", "objectA");
        test.put((K) "keyX", "object0");
        test.putAll(original);

        assertEquals(3, test.size());
        assertEquals(4, test.totalSize());
        assertEquals(1, test.getCollection("keyA").size());
        assertEquals(2, test.getCollection("keyX").size());
        assertEquals(1, test.getCollection("keyY").size());
        assertEquals(true, test.containsValue("objectA"));
        assertEquals(true, test.containsValue("object0"));
        assertEquals(true, test.containsValue("object1"));
        assertEquals(true, test.containsValue("object2"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testPutAll_KeyCollection
    public void testPutAll_KeyCollection() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        Collection<V> coll = (Collection<V>) Arrays.asList("X", "Y", "Z");

        assertEquals(true, map.putAll((K) "A", coll));
        assertEquals(3, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));

        assertEquals(false, map.putAll((K) "A", null));
        assertEquals(3, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));

        assertEquals(false, map.putAll((K) "A", new ArrayList<V>()));
        assertEquals(3, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));

        coll = (Collection<V>) Arrays.asList("M");
        assertEquals(true, map.putAll((K) "A", coll));
        assertEquals(4, map.size("A"));
        assertEquals(true, map.containsValue("A", "X"));
        assertEquals(true, map.containsValue("A", "Y"));
        assertEquals(true, map.containsValue("A", "Z"));
        assertEquals(true, map.containsValue("A", "M"));
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testRemove_KeyItem
    public void testRemove_KeyItem() {
        final MultiValueMap<K, V> map = new MultiValueMap<K, V>();
        map.put((K) "A", "AA");
        map.put((K) "A", "AB");
        map.put((K) "A", "AC");
        assertEquals(false, map.removeMapping("C", "CA"));
        assertEquals(false, map.removeMapping("A", "AD"));
        assertEquals(true, map.removeMapping("A", "AC"));
        assertEquals(true, map.removeMapping("A", "AB"));
        assertEquals(true, map.removeMapping("A", "AA"));
        assertEquals(new MultiValueMap<K, V>(), map);
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testEmptyMapCompatibility
    public void testEmptyMapCompatibility() throws Exception {
        final Map<?,?> map = makeEmptyMap();
        final Map<?,?> map2 = (Map<?,?>) readExternalFormFromDisk(getCanonicalEmptyCollectionName(map));
        assertEquals("Map is empty", 0, map2.size());
    }

// org.apache.commons.collections4.map.MultiValueMapTest::testFullMapCompatibility
    public void testFullMapCompatibility() throws Exception {
        final Map<?,?> map = (Map<?,?>) makeObject();
        final Map<?,?> map2 = (Map<?,?>) readExternalFormFromDisk(getCanonicalFullCollectionName(map));
        assertEquals("Map is the right size", map.size(), map2.size());
        for (final Object key : map.keySet()) {
            assertEquals( "Map had inequal elements", map.get(key), map2.get(key) );
            map2.remove(key);
        }
        assertEquals("Map had extra values", 0, map2.size());
    }

// org.apache.commons.collections4.set.ListOrderedSetTest::testOrdering
    public void testOrdering() {
        final ListOrderedSet<E> set = setupSet();
        Iterator<E> it = set.iterator();

        for (int i = 0; i < 10; i++) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }

        for (int i = 0; i < 10; i += 2) {
            assertTrue("Must be able to remove int",
                       set.remove(Integer.toString(i)));
        }

        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong after remove ",
                         Integer.toString(i), it.next());
        }

        for (int i = 0; i < 10; i++) {
            set.add((E) Integer.toString(i));
        }

        assertEquals("Size of set is wrong!", 10, set.size());

        it = set.iterator();
        for (int i = 1; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
        for (int i = 0; i < 10; i += 2) {
            assertEquals("Sequence is wrong", Integer.toString(i), it.next());
        }
    }

// org.apache.commons.collections4.set.ListOrderedSetTest::testListAddRemove
    public void testListAddRemove() {
        final ListOrderedSet<E> set = makeObject();
        final List<E> view = set.asList();
        set.add((E) ZERO);
        set.add((E) ONE);
        set.add((E) TWO);

        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));
        assertEquals(3, view.size());
        assertSame(ZERO, view.get(0));
        assertSame(ONE, view.get(1));
        assertSame(TWO, view.get(2));

        assertEquals(0, set.indexOf(ZERO));
        assertEquals(1, set.indexOf(ONE));
        assertEquals(2, set.indexOf(TWO));

        set.remove(1);
        assertEquals(2, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(TWO, set.get(1));
        assertEquals(2, view.size());
        assertSame(ZERO, view.get(0));
        assertSame(TWO, view.get(1));
    }

// org.apache.commons.collections4.set.ListOrderedSetTest::testListAddIndexed
    public void testListAddIndexed() {
        final ListOrderedSet<E> set = makeObject();
        set.add((E) ZERO);
        set.add((E) TWO);

        set.add(1, (E) ONE);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        set.add(0, (E) ONE);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        final List<E> list = new ArrayList<E>();
        list.add((E) ZERO);
        list.add((E) TWO);

        set.addAll(0, list);
        assertEquals(3, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(ONE, set.get(1));
        assertSame(TWO, set.get(2));

        list.add(0, (E) THREE); 
        set.remove(TWO); 
        set.addAll(1, list);
        assertEquals(4, set.size());
        assertSame(ZERO, set.get(0));
        assertSame(THREE, set.get(1));
        assertSame(TWO, set.get(2));
        assertSame(ONE, set.get(3));
    }

// org.apache.commons.collections4.set.ListOrderedSetTest::testListAddReplacing
    public void testListAddReplacing() {
        final ListOrderedSet<E> set = makeObject();
        final A a = new A();
        final B b = new B();
        set.add((E) a);
        assertEquals(1, set.size());
        set.add((E) b); 
        assertEquals(1, set.size());
        assertSame(a, set.decorated().iterator().next());
        assertSame(a, set.iterator().next());
        assertSame(a, set.get(0));
        assertSame(a, set.asList().get(0));
    }

// org.apache.commons.collections4.set.ListOrderedSetTest::testRetainAll
    public void testRetainAll() {
        final List<E> list = new ArrayList<E>(10);
        final Set<E> set = new HashSet<E>(10);
        final ListOrderedSet<E> orderedSet = ListOrderedSet.listOrderedSet(set, list);
        for (int i = 0; i < 10; ++i) {
            orderedSet.add((E) Integer.valueOf(10 - i - 1));
        }

        final Collection<E> retained = new ArrayList<E>(5);
        for (int i = 0; i < 5; ++i) {
            retained.add((E) Integer.valueOf(i * 2));
        }

        assertTrue(orderedSet.retainAll(retained));
        assertEquals(5, orderedSet.size());
        
        assertEquals(Integer.valueOf(8), orderedSet.get(0));
        assertEquals(Integer.valueOf(6), orderedSet.get(1));
        assertEquals(Integer.valueOf(4), orderedSet.get(2));
        assertEquals(Integer.valueOf(2), orderedSet.get(3));
        assertEquals(Integer.valueOf(0), orderedSet.get(4));
    }

// org.apache.commons.collections4.set.ListOrderedSetTest::testDuplicates
    public void testDuplicates() {
        final List<E> list = new ArrayList<E>(10);
        list.add((E) Integer.valueOf(1));
        list.add((E) Integer.valueOf(2));
        list.add((E) Integer.valueOf(3));
        list.add((E) Integer.valueOf(1));

        final ListOrderedSet<E> orderedSet = ListOrderedSet.listOrderedSet(list);

        assertEquals(3, orderedSet.size());
        assertEquals(3, IteratorUtils.toArray(orderedSet.iterator()).length);

        
        assertEquals(Integer.valueOf(1), orderedSet.get(0));
        assertEquals(Integer.valueOf(2), orderedSet.get(1));
        assertEquals(Integer.valueOf(3), orderedSet.get(2));
    }

// org.apache.commons.collections4.set.ListOrderedSetTest::testDecorator
    public void testDecorator() {
        try {
            ListOrderedSet.listOrderedSet((List<E>) null);
            fail();
        } catch (final IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet((Set<E>) null);
            fail();
        } catch (final IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet(null, null);
            fail();
        } catch (final IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet(new HashSet<E>(), null);
            fail();
        } catch (final IllegalArgumentException ex) {
        }
        try {
            ListOrderedSet.listOrderedSet(null, new ArrayList<E>());
            fail();
        } catch (final IllegalArgumentException ex) {
        }
    }
