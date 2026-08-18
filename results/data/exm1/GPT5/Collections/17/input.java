// buggy code
    public EqualPredicate(T object) {
        // do not use the DefaultEquator to keep backwards compatibility
        // the DefaultEquator returns also true if the two object references are equal
        this(object, new DefaultEquator<T>());
    }

    public boolean evaluate(T object) {
            return equator.equate(iValue, object);
    }

// relevant test
// org.apache.commons.collections.TestClosureUtils::testExceptionClosure
    public void testExceptionClosure() {
        assertNotNull(ClosureUtils.exceptionClosure());
        assertSame(ClosureUtils.exceptionClosure(), ClosureUtils.exceptionClosure());
        try {
            ClosureUtils.exceptionClosure().execute(null);
        } catch (FunctorException ex) {
            try {
                ClosureUtils.exceptionClosure().execute(cString);
            } catch (FunctorException ex2) {
                return;
            }
        }
        fail();
    }

// org.apache.commons.collections.TestClosureUtils::testNopClosure
    public void testNopClosure() {
        StringBuilder buf = new StringBuilder("Hello");
        ClosureUtils.nopClosure().execute(null);
        assertEquals("Hello", buf.toString());
        ClosureUtils.nopClosure().execute("Hello");
        assertEquals("Hello", buf.toString());
    }

// org.apache.commons.collections.TestClosureUtils::testInvokeClosure
    public void testInvokeClosure() {
        StringBuffer buf = new StringBuffer("Hello"); 
        ClosureUtils.invokerClosure("reverse").execute(buf);
        assertEquals("olleH", buf.toString());
        buf = new StringBuffer("Hello");
        ClosureUtils.invokerClosure("setLength", new Class[] {Integer.TYPE}, new Object[] {new Integer(2)}).execute(buf);
        assertEquals("He", buf.toString());
    }

// org.apache.commons.collections.TestClosureUtils::testForClosure
    public void testForClosure() {
        MockClosure<Object> cmd = new MockClosure<Object>();
        ClosureUtils.forClosure(5, cmd).execute(null);
        assertEquals(5, cmd.count);
        assertSame(NOPClosure.INSTANCE, ClosureUtils.forClosure(0, new MockClosure<Object>()));
        assertSame(NOPClosure.INSTANCE, ClosureUtils.forClosure(-1, new MockClosure<Object>()));
        assertSame(NOPClosure.INSTANCE, ClosureUtils.forClosure(1, null));
        assertSame(NOPClosure.INSTANCE, ClosureUtils.forClosure(3, null));
        assertSame(cmd, ClosureUtils.forClosure(1, cmd));
    }

// org.apache.commons.collections.TestClosureUtils::testWhileClosure
    public void testWhileClosure() {
        MockClosure<Object> cmd = new MockClosure<Object>();
        ClosureUtils.whileClosure(FalsePredicate.falsePredicate(), cmd).execute(null);
        assertEquals(0, cmd.count);

        cmd = new MockClosure<Object>();
        ClosureUtils.whileClosure(PredicateUtils.uniquePredicate(), cmd).execute(null);
        assertEquals(1, cmd.count);

        try {
            ClosureUtils.whileClosure(null, ClosureUtils.nopClosure());
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.whileClosure(FalsePredicate.falsePredicate(), null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.whileClosure(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestClosureUtils::testDoWhileClosure
    public void testDoWhileClosure() {
        MockClosure<Object> cmd = new MockClosure<Object>();
        ClosureUtils.doWhileClosure(cmd, FalsePredicate.falsePredicate()).execute(null);
        assertEquals(1, cmd.count);

        cmd = new MockClosure<Object>();
        ClosureUtils.doWhileClosure(cmd, PredicateUtils.uniquePredicate()).execute(null);
        assertEquals(2, cmd.count);

        try {
            ClosureUtils.doWhileClosure(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestClosureUtils::testChainedClosure
    public void testChainedClosure() {
        MockClosure<Object> a = new MockClosure<Object>();
        MockClosure<Object> b = new MockClosure<Object>();
        ClosureUtils.chainedClosure(a, b).execute(null);
        assertEquals(1, a.count);
        assertEquals(1, b.count);

        a = new MockClosure<Object>();
        b = new MockClosure<Object>();
        ClosureUtils.<Object>chainedClosure(new Closure[] {a, b, a}).execute(null);
        assertEquals(2, a.count);
        assertEquals(1, b.count);

        a = new MockClosure<Object>();
        b = new MockClosure<Object>();
        Collection<Closure<Object>> coll = new ArrayList<Closure<Object>>();
        coll.add(b);
        coll.add(a);
        coll.add(b);
        ClosureUtils.<Object>chainedClosure(coll).execute(null);
        assertEquals(1, a.count);
        assertEquals(2, b.count);

        assertSame(NOPClosure.INSTANCE, ClosureUtils.<Object>chainedClosure(new Closure[0]));
        assertSame(NOPClosure.INSTANCE, ClosureUtils.<Object>chainedClosure(Collections.<Closure<Object>>emptyList()));

        try {
            ClosureUtils.chainedClosure(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.<Object>chainedClosure((Closure[]) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.<Object>chainedClosure((Collection<Closure<Object>>) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.<Object>chainedClosure(new Closure[] {null, null});
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            coll = new ArrayList<Closure<Object>>();
            coll.add(null);
            coll.add(null);
            ClosureUtils.chainedClosure(coll);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestClosureUtils::testIfClosure
    public void testIfClosure() {
        MockClosure<Object> a = new MockClosure<Object>();
        MockClosure<Object> b = null;
        ClosureUtils.ifClosure(TruePredicate.truePredicate(), a).execute(null);
        assertEquals(1, a.count);

        a = new MockClosure<Object>();
        ClosureUtils.ifClosure(FalsePredicate.<Object>falsePredicate(), a).execute(null);
        assertEquals(0, a.count);

        a = new MockClosure<Object>();
        b = new MockClosure<Object>();
        ClosureUtils.ifClosure(TruePredicate.<Object>truePredicate(), a, b).execute(null);
        assertEquals(1, a.count);
        assertEquals(0, b.count);

        a = new MockClosure<Object>();
        b = new MockClosure<Object>();
        ClosureUtils.ifClosure(FalsePredicate.<Object>falsePredicate(), a, b).execute(null);
        assertEquals(0, a.count);
        assertEquals(1, b.count);
    }

// org.apache.commons.collections.TestClosureUtils::testSwitchClosure
    public void testSwitchClosure() {
        MockClosure<String> a = new MockClosure<String>();
        MockClosure<String> b = new MockClosure<String>();
        ClosureUtils.<String>switchClosure(
            new Predicate[] { EqualPredicate.equalPredicate("HELLO"), EqualPredicate.equalPredicate("THERE") },
            new Closure[] { a, b }).execute("WELL");
        assertEquals(0, a.count);
        assertEquals(0, b.count);

        a.reset();
        b.reset();
        ClosureUtils.<String>switchClosure(
            new Predicate[] { EqualPredicate.equalPredicate("HELLO"), EqualPredicate.equalPredicate("THERE") },
            new Closure[] { a, b }).execute("HELLO");
        assertEquals(1, a.count);
        assertEquals(0, b.count);

        a.reset();
        b.reset();
        MockClosure<String> c = new MockClosure<String>();
        ClosureUtils.<String>switchClosure(
            new Predicate[] { EqualPredicate.equalPredicate("HELLO"), EqualPredicate.equalPredicate("THERE") },
            new Closure[] { a, b }, c).execute("WELL");
        assertEquals(0, a.count);
        assertEquals(0, b.count);
        assertEquals(1, c.count);

        a.reset();
        b.reset();
        Map<Predicate<String>, Closure<String>> map = new HashMap<Predicate<String>, Closure<String>>();
        map.put(EqualPredicate.equalPredicate("HELLO"), a);
        map.put(EqualPredicate.equalPredicate("THERE"), b);
        ClosureUtils.<String>switchClosure(map).execute(null);
        assertEquals(0, a.count);
        assertEquals(0, b.count);

        a.reset();
        b.reset();
        map.clear();
        map.put(EqualPredicate.equalPredicate("HELLO"), a);
        map.put(EqualPredicate.equalPredicate("THERE"), b);
        ClosureUtils.switchClosure(map).execute("THERE");
        assertEquals(0, a.count);
        assertEquals(1, b.count);

        a.reset();
        b.reset();
        c.reset();
        map.clear();
        map.put(EqualPredicate.equalPredicate("HELLO"), a);
        map.put(EqualPredicate.equalPredicate("THERE"), b);
        map.put(null, c);
        ClosureUtils.switchClosure(map).execute("WELL");
        assertEquals(0, a.count);
        assertEquals(0, b.count);
        assertEquals(1, c.count);

        assertEquals(NOPClosure.INSTANCE, ClosureUtils.<String>switchClosure(new Predicate[0], new Closure[0]));
        assertEquals(NOPClosure.INSTANCE, ClosureUtils.<String>switchClosure(new HashMap<Predicate<String>, Closure<String>>()));
        map.clear();
        map.put(null, null);
        assertEquals(NOPClosure.INSTANCE, ClosureUtils.switchClosure(map));

        try {
            ClosureUtils.switchClosure(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.<String>switchClosure((Predicate<String>[]) null, (Closure<String>[]) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.<String>switchClosure((Map<Predicate<String>, Closure<String>>) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.<String>switchClosure(new Predicate[2], new Closure[2]);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            ClosureUtils.<String>switchClosure(
                    new Predicate[] { TruePredicate.<String>truePredicate() },
                    new Closure[] { a, b });
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestClosureUtils::testSwitchMapClosure
    public void testSwitchMapClosure() {
        MockClosure<String> a = new MockClosure<String>();
        MockClosure<String> b = new MockClosure<String>();
        Map<String, Closure<String>> map = new HashMap<String, Closure<String>>();
        map.put("HELLO", a);
        map.put("THERE", b);
        ClosureUtils.switchMapClosure(map).execute(null);
        assertEquals(0, a.count);
        assertEquals(0, b.count);

        a.reset();
        b.reset();
        map.clear();
        map.put("HELLO", a);
        map.put("THERE", b);
        ClosureUtils.switchMapClosure(map).execute("THERE");
        assertEquals(0, a.count);
        assertEquals(1, b.count);

        a.reset();
        b.reset();
        map.clear();
        MockClosure<String> c = new MockClosure<String>();
        map.put("HELLO", a);
        map.put("THERE", b);
        map.put(null, c);
        ClosureUtils.switchMapClosure(map).execute("WELL");
        assertEquals(0, a.count);
        assertEquals(0, b.count);
        assertEquals(1, c.count);

        assertEquals(NOPClosure.INSTANCE, ClosureUtils.switchMapClosure(new HashMap<String, Closure<String>>()));

        try {
            ClosureUtils.switchMapClosure(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestClosureUtils::testTransformerClosure
    public void testTransformerClosure() {
        MockTransformer<Object> mock = new MockTransformer<Object>();
        Closure<Object> closure = ClosureUtils.asClosure(mock);
        closure.execute(null);
        assertEquals(1, mock.count);
        closure.execute(null);
        assertEquals(2, mock.count);

        assertEquals(ClosureUtils.nopClosure(), ClosureUtils.asClosure(null));
    }

// org.apache.commons.collections.TestClosureUtils::testSingletonPatternInSerialization
    public void testSingletonPatternInSerialization() {
        final Object[] singletones = new Object[] {
                ExceptionClosure.INSTANCE,
                NOPClosure.INSTANCE,
        };

        for (final Object original : singletones) {
            TestUtils.assertSameAfterSerialization(
                    "Singletone patern broken for " + original.getClass(),
                    original
            );
        }
    }

// org.apache.commons.collections.TestCollectionUtils::getCardinalityMap
    public void getCardinalityMap() {
        Map<Number, Integer> freqA = CollectionUtils.<Number>getCardinalityMap(iterableA);
        assertEquals(1, (int) freqA.get(1));
        assertEquals(2, (int) freqA.get(2));
        assertEquals(3, (int) freqA.get(3));
        assertEquals(4, (int) freqA.get(4));
        assertNull(freqA.get(5));

        Map<Long, Integer> freqB = CollectionUtils.getCardinalityMap(iterableB);
        assertNull(freqB.get(1L));
        assertEquals(4, (int) freqB.get(2L));
        assertEquals(3, (int) freqB.get(3L));
        assertEquals(2, (int) freqB.get(4L));
        assertEquals(1, (int) freqB.get(5L));
    }

// org.apache.commons.collections.TestCollectionUtils::cardinality
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

        Set<String> set = new HashSet<String>();
        set.add("A");
        set.add("C");
        set.add("E");
        set.add("E");
        assertEquals(1, CollectionUtils.cardinality("A", set));
        assertEquals(0, CollectionUtils.cardinality("B", set));
        assertEquals(1, CollectionUtils.cardinality("C", set));
        assertEquals(0, CollectionUtils.cardinality("D", set));
        assertEquals(1, CollectionUtils.cardinality("E", set));

        Bag<String> bag = new HashBag<String>();
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

// org.apache.commons.collections.TestCollectionUtils::cardinalityOfNull
    public void cardinalityOfNull() {
        List<String> list = new ArrayList<String>();
        assertEquals(0, CollectionUtils.cardinality(null, list));
        {
            Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertNull(freq.get(null));
        }
        list.add("A");
        assertEquals(0, CollectionUtils.cardinality(null, list));
        {
            Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertNull(freq.get(null));
        }
        list.add(null);
        assertEquals(1, CollectionUtils.cardinality(null, list));
        {
            Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(1), freq.get(null));
        }
        list.add("B");
        assertEquals(1, CollectionUtils.cardinality(null, list));
        {
            Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(1), freq.get(null));
        }
        list.add(null);
        assertEquals(2, CollectionUtils.cardinality(null, list));
        {
            Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(2), freq.get(null));
        }
        list.add("B");
        assertEquals(2, CollectionUtils.cardinality(null, list));
        {
            Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(2), freq.get(null));
        }
        list.add(null);
        assertEquals(3, CollectionUtils.cardinality(null, list));
        {
            Map<String, Integer> freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(Integer.valueOf(3), freq.get(null));
        }
    }

// org.apache.commons.collections.TestCollectionUtils::containsAny
    public void containsAny() {
        Collection<String> empty = new ArrayList<String>(0);
        Collection<String> one = new ArrayList<String>(1);
        one.add("1");
        Collection<String> two = new ArrayList<String>(1);
        two.add("2");
        Collection<String> three = new ArrayList<String>(1);
        three.add("3");
        Collection<String> odds = new ArrayList<String>(2);
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

// org.apache.commons.collections.TestCollectionUtils::union
    public void union() {
        Collection<Integer> col = CollectionUtils.union(iterableA, iterableC);
        Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq.get(1));
        assertEquals(Integer.valueOf(4), freq.get(2));
        assertEquals(Integer.valueOf(3), freq.get(3));
        assertEquals(Integer.valueOf(4), freq.get(4));
        assertEquals(Integer.valueOf(1), freq.get(5));

        Collection<Number> col2 = CollectionUtils.union(collectionC2, iterableA);
        Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(Integer.valueOf(1), freq2.get(1));
        assertEquals(Integer.valueOf(4), freq2.get(2));
        assertEquals(Integer.valueOf(3), freq2.get(3));
        assertEquals(Integer.valueOf(4), freq2.get(4));
        assertEquals(Integer.valueOf(1), freq2.get(5));
    }

// org.apache.commons.collections.TestCollectionUtils::intersection
    public void intersection() {
        Collection<Integer> col = CollectionUtils.intersection(iterableA, iterableC);
        Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertNull(freq.get(1));
        assertEquals(Integer.valueOf(2), freq.get(2));
        assertEquals(Integer.valueOf(3), freq.get(3));
        assertEquals(Integer.valueOf(2), freq.get(4));
        assertNull(freq.get(5));

        Collection<Number> col2 = CollectionUtils.intersection(collectionC2, collectionA);
        Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertNull(freq2.get(1));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertEquals(Integer.valueOf(3), freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(4));
        assertNull(freq2.get(5));
    }

// org.apache.commons.collections.TestCollectionUtils::disjunction
    public void disjunction() {
        Collection<Integer> col = CollectionUtils.disjunction(iterableA, iterableC);
        Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq.get(1));
        assertEquals(Integer.valueOf(2), freq.get(2));
        assertNull(freq.get(3));
        assertEquals(Integer.valueOf(2), freq.get(4));
        assertEquals(Integer.valueOf(1), freq.get(5));

        Collection<Number> col2 = CollectionUtils.disjunction(collectionC2, collectionA);
        Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(Integer.valueOf(1), freq2.get(1));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertNull(freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(4));
        assertEquals(Integer.valueOf(1), freq2.get(5));
    }

// org.apache.commons.collections.TestCollectionUtils::testDisjunctionAsUnionMinusIntersection
    public void testDisjunctionAsUnionMinusIntersection() {
        Collection<Number> dis = CollectionUtils.<Number>disjunction(collectionA, collectionC);
        Collection<Number> un = CollectionUtils.<Number>union(collectionA, collectionC);
        Collection<Number> inter = CollectionUtils.<Number>intersection(collectionA, collectionC);
        assertTrue(CollectionUtils.isEqualCollection(dis, CollectionUtils.subtract(un, inter)));
    }

// org.apache.commons.collections.TestCollectionUtils::testDisjunctionAsSymmetricDifference
    public void testDisjunctionAsSymmetricDifference() {
        Collection<Number> dis = CollectionUtils.<Number>disjunction(collectionA, collectionC);
        Collection<Number> amb = CollectionUtils.<Number>subtract(collectionA, collectionC);
        Collection<Number> bma = CollectionUtils.<Number>subtract(collectionC, collectionA);
        assertTrue(CollectionUtils.isEqualCollection(dis, CollectionUtils.union(amb, bma)));
    }

// org.apache.commons.collections.TestCollectionUtils::testSubtract
    public void testSubtract() {
        Collection<Integer> col = CollectionUtils.subtract(iterableA, iterableC);
        Map<Integer, Integer> freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq.get(1));
        assertNull(freq.get(2));
        assertNull(freq.get(3));
        assertEquals(Integer.valueOf(2), freq.get(4));
        assertNull(freq.get(5));

        Collection<Number> col2 = CollectionUtils.subtract(collectionC2, collectionA);
        Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(Integer.valueOf(1), freq2.get(5));
        assertNull(freq2.get(4));
        assertNull(freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertNull(freq2.get(1));
    }

// org.apache.commons.collections.TestCollectionUtils::testSubtractWithPredicate
    public void testSubtractWithPredicate() {
        
        Predicate<Number> predicate = new Predicate<Number>() {
            public boolean evaluate(Number n) {
                return n.longValue() > 3L;
            }
        };
        
        Collection<Number> col = CollectionUtils.subtract(iterableA, collectionC, predicate);
        Map<Number, Integer> freq2 = CollectionUtils.getCardinalityMap(col);
        assertEquals(Integer.valueOf(1), freq2.get(1));
        assertEquals(Integer.valueOf(2), freq2.get(2));
        assertEquals(Integer.valueOf(3), freq2.get(3));
        assertEquals(Integer.valueOf(2), freq2.get(4));
        assertNull(freq2.get(5));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsSubCollectionOfSelf
    public void testIsSubCollectionOfSelf() {
        assertTrue(CollectionUtils.isSubCollection(collectionA, collectionA));
        assertTrue(CollectionUtils.isSubCollection(collectionB, collectionB));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsSubCollection
    public void testIsSubCollection() {
        assertTrue(!CollectionUtils.isSubCollection(collectionA, collectionC));
        assertTrue(!CollectionUtils.isSubCollection(collectionC, collectionA));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsSubCollection2
    public void testIsSubCollection2() {
        Collection<Integer> c = new ArrayList<Integer>();
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

// org.apache.commons.collections.TestCollectionUtils::testIsEqualCollectionToSelf
    public void testIsEqualCollectionToSelf() {
        assertTrue(CollectionUtils.isEqualCollection(collectionA, collectionA));
        assertTrue(CollectionUtils.isEqualCollection(collectionB, collectionB));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEqualCollection
    public void testIsEqualCollection() {
        assertTrue(!CollectionUtils.isEqualCollection(collectionA, collectionC));
        assertTrue(!CollectionUtils.isEqualCollection(collectionC, collectionA));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEqualCollectionReturnsFalse
    public void testIsEqualCollectionReturnsFalse() {
        List<Integer> b = new ArrayList<Integer>(collectionA);
        
        b.remove(1);
        b.add(5);
        assertFalse(CollectionUtils.isEqualCollection(collectionA, b));
        assertFalse(CollectionUtils.isEqualCollection(b, collectionA));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEqualCollection2
    public void testIsEqualCollection2() {
        Collection<String> a = new ArrayList<String>();
        Collection<String> b = new ArrayList<String>();
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

// org.apache.commons.collections.TestCollectionUtils::testIsProperSubCollection
    public void testIsProperSubCollection() {
        Collection<String> a = new ArrayList<String>();
        Collection<String> b = new ArrayList<String>();
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

// org.apache.commons.collections.TestCollectionUtils::find
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

// org.apache.commons.collections.TestCollectionUtils::forAllDo
    public void forAllDo() {
        Closure<List<? extends Number>> testClosure = ClosureUtils.invokerClosure("clear");
        Collection<List<? extends Number>> col = new ArrayList<List<? extends Number>>();
        col.add(collectionA);
        col.add(collectionB);
        Closure<List<? extends Number>> resultClosure = CollectionUtils.forAllDo(col, testClosure);
        assertSame(testClosure, resultClosure);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        resultClosure = CollectionUtils.<List<? extends Number>,Closure<List<? extends Number>>>forAllDo(col, null);
        assertNull(resultClosure);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        resultClosure = CollectionUtils.forAllDo(null, testClosure);
        col.add(null);
        
        CollectionUtils.forAllDo(col, testClosure);
    }

// org.apache.commons.collections.TestCollectionUtils::forAllDoFailure
    public void forAllDoFailure() {
        Closure<String> testClosure = ClosureUtils.invokerClosure("clear");
        Collection<String> col = new ArrayList<String>();
        col.add("x");
        CollectionUtils.forAllDo(col, testClosure);
    }

// org.apache.commons.collections.TestCollectionUtils::getFromMap
    public void getFromMap() {
        
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("zeroKey", "zero");
        expected.put("oneKey", "one");

        Map<String, String> found = new HashMap<String, String>();
        Map.Entry<String, String> entry = CollectionUtils.get(expected, 0);
        found.put(entry.getKey(), entry.getValue());
        entry = CollectionUtils.get(expected, 1);
        found.put(entry.getKey(), entry.getValue());
        assertEquals(expected, found);

        
        try {
            CollectionUtils.get(expected, 2);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            
        }
        try {
            CollectionUtils.get(expected, -2);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            
        }

        
        SortedMap<String, String> map = new TreeMap<String, String>();
        map.put("zeroKey", "zero");
        map.put("oneKey", "one");
        Map.Entry<String, String> test = CollectionUtils.get(map, 1);
        assertEquals("zeroKey", test.getKey());
        assertEquals("zero", test.getValue());
        test = CollectionUtils.get(map, 0);
        assertEquals("oneKey", test.getKey());
        assertEquals("one", test.getValue());
    }

// org.apache.commons.collections.TestCollectionUtils::getFromList
    public void getFromList() throws Exception {
        
        List<String> list = createMock(List.class);
        expect(list.get(0)).andReturn("zero");
        expect(list.get(1)).andReturn("one");
        replay();
        String string = CollectionUtils.get(list, 0);
        assertEquals("zero", string);
        assertEquals("one", CollectionUtils.get(list, 1));
        
        CollectionUtils.get(new ArrayList<Object>(), 2);
    }

// org.apache.commons.collections.TestCollectionUtils::getFromIterator
    public void getFromIterator() throws Exception {
        
        Iterator<Integer> iterator = iterableA.iterator();
        assertEquals(1, (int) CollectionUtils.get(iterator, 0));
        iterator = iterableA.iterator();
        assertEquals(2, (int) CollectionUtils.get(iterator, 1));

        
        try {
            CollectionUtils.get(iterator, 10);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            
        }
        assertTrue(!iterator.hasNext());
    }

// org.apache.commons.collections.TestCollectionUtils::getFromEnumeration
    public void getFromEnumeration() throws Exception {
        
        Vector<String> vector = new Vector<String>();
        vector.addElement("zero");
        vector.addElement("one");
        Enumeration<String> en = vector.elements();
        assertEquals("zero", CollectionUtils.get(en, 0));
        en = vector.elements();
        assertEquals("one", CollectionUtils.get(en, 1));

        
        try {
            CollectionUtils.get(en, 3);
            fail("Expecting IndexOutOfBoundsException.");
        } catch (IndexOutOfBoundsException e) {
            
        }
        assertTrue(!en.hasMoreElements());
    }

// org.apache.commons.collections.TestCollectionUtils::getFromIterable
    public void getFromIterable() throws Exception {
        
        Bag<String> bag = new HashBag<String>();
        bag.add("element", 1);
        assertEquals("element", CollectionUtils.get(bag, 0));

        
        CollectionUtils.get(bag, 1);
    }

// org.apache.commons.collections.TestCollectionUtils::getFromObjectArray
    public void getFromObjectArray() throws Exception {
        
        Object[] objArray = new Object[2];
        objArray[0] = "zero";
        objArray[1] = "one";
        assertEquals("zero", CollectionUtils.get(objArray, 0));
        assertEquals("one", CollectionUtils.get(objArray, 1));

        
        
        CollectionUtils.get(objArray, 2);
    }

// org.apache.commons.collections.TestCollectionUtils::getFromPrimativeArray
    public void getFromPrimativeArray() throws Exception {
        
        int[] array = new int[2];
        array[0] = 10;
        array[1] = 20;
        assertEquals(10, CollectionUtils.get(array, 0));
        assertEquals(20, CollectionUtils.get(array, 1));

        
        
        CollectionUtils.get(array, 2);
    }

// org.apache.commons.collections.TestCollectionUtils::getFromObject
    public void getFromObject() throws Exception {
        
        Object obj = new Object();
        CollectionUtils.get(obj, 0);
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_List
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

// org.apache.commons.collections.TestCollectionUtils::testSize_Map
    public void testSize_Map() {
        Map<String, String> map = new HashMap<String, String>();
        assertEquals(0, CollectionUtils.size(map));
        map.put("1", "a");
        assertEquals(1, CollectionUtils.size(map));
        map.put("2", "b");
        assertEquals(2, CollectionUtils.size(map));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_Array
    public void testSize_Array() {
        Object[] objectArray = new Object[0];
        assertEquals(0, CollectionUtils.size(objectArray));

        String[] stringArray = new String[3];
        assertEquals(3, CollectionUtils.size(stringArray));
        stringArray[0] = "a";
        stringArray[1] = "b";
        stringArray[2] = "c";
        assertEquals(3, CollectionUtils.size(stringArray));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_PrimitiveArray
    public void testSize_PrimitiveArray() {
        int[] intArray = new int[0];
        assertEquals(0, CollectionUtils.size(intArray));

        double[] doubleArray = new double[3];
        assertEquals(3, CollectionUtils.size(doubleArray));
        doubleArray[0] = 0.0d;
        doubleArray[1] = 1.0d;
        doubleArray[2] = 2.5d;
        assertEquals(3, CollectionUtils.size(doubleArray));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_Enumeration
    public void testSize_Enumeration() {
        Vector<String> list = new Vector<String>();
        assertEquals(0, CollectionUtils.size(list.elements()));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list.elements()));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list.elements()));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_Iterator
    public void testSize_Iterator() {
        List<String> list = new ArrayList<String>();
        assertEquals(0, CollectionUtils.size(list.iterator()));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list.iterator()));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list.iterator()));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_Other
    public void testSize_Other() {
        CollectionUtils.size("not a list");
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Null
    public void testSizeIsEmpty_Null() {
        assertEquals(true, CollectionUtils.sizeIsEmpty(null));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_List
    public void testSizeIsEmpty_List() {
        List<String> list = new ArrayList<String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Map
    public void testSizeIsEmpty_Map() {
        Map<String, String> map = new HashMap<String, String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(map));
        map.put("1", "a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(map));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Array
    public void testSizeIsEmpty_Array() {
        Object[] objectArray = new Object[0];
        assertEquals(true, CollectionUtils.sizeIsEmpty(objectArray));

        String[] stringArray = new String[3];
        assertEquals(false, CollectionUtils.sizeIsEmpty(stringArray));
        stringArray[0] = "a";
        stringArray[1] = "b";
        stringArray[2] = "c";
        assertEquals(false, CollectionUtils.sizeIsEmpty(stringArray));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_PrimitiveArray
    public void testSizeIsEmpty_PrimitiveArray() {
        int[] intArray = new int[0];
        assertEquals(true, CollectionUtils.sizeIsEmpty(intArray));

        double[] doubleArray = new double[3];
        assertEquals(false, CollectionUtils.sizeIsEmpty(doubleArray));
        doubleArray[0] = 0.0d;
        doubleArray[1] = 1.0d;
        doubleArray[2] = 2.5d;
        assertEquals(false, CollectionUtils.sizeIsEmpty(doubleArray));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Enumeration
    public void testSizeIsEmpty_Enumeration() {
        Vector<String> list = new Vector<String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list.elements()));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list.elements()));
        Enumeration<String> en = list.elements();
        en.nextElement();
        assertEquals(true, CollectionUtils.sizeIsEmpty(en));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Iterator
    public void testSizeIsEmpty_Iterator() {
        List<String> list = new ArrayList<String>();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list.iterator()));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list.iterator()));
        Iterator<String> it = list.iterator();
        it.next();
        assertEquals(true, CollectionUtils.sizeIsEmpty(it));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Other
    public void testSizeIsEmpty_Other() {
        try {
            CollectionUtils.sizeIsEmpty("not a list");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEmptyWithEmptyCollection
    public void testIsEmptyWithEmptyCollection() {
        Collection<Object> coll = new ArrayList<Object>();
        assertEquals(true, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEmptyWithNonEmptyCollection
    public void testIsEmptyWithNonEmptyCollection() {
        Collection<String> coll = new ArrayList<String>();
        coll.add("item");
        assertEquals(false, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEmptyWithNull
    public void testIsEmptyWithNull() {
        Collection<?> coll = null;
        assertEquals(true, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsNotEmptyWithEmptyCollection
    public void testIsNotEmptyWithEmptyCollection() {
        Collection<Object> coll = new ArrayList<Object>();
        assertEquals(false, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsNotEmptyWithNonEmptyCollection
    public void testIsNotEmptyWithNonEmptyCollection() {
        Collection<String> coll = new ArrayList<String>();
        coll.add("item");
        assertEquals(true, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsNotEmptyWithNull
    public void testIsNotEmptyWithNull() {
        Collection<?> coll = null;
        assertEquals(false, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::filter
    public void filter() {
        List<Integer> ints = new ArrayList<Integer>();
        ints.add(1);
        ints.add(2);
        ints.add(3);
        ints.add(3);
        Iterable<Integer> iterable = ints;
        assertTrue(CollectionUtils.filter(iterable, EQUALS_TWO));
        assertEquals(1, (int) ints.size());
        assertEquals(2, (int) ints.get(0));
    }

// org.apache.commons.collections.TestCollectionUtils::filterNullParameters
    public void filterNullParameters() throws Exception {
        List<Long> longs = Collections.nCopies(4, 10L);
        assertFalse(CollectionUtils.filter(longs, null));
        assertEquals(4, longs.size());
        assertFalse(CollectionUtils.filter(null, EQUALS_TWO));
        assertEquals(4, longs.size());
        assertFalse(CollectionUtils.filter(null, null));
        assertEquals(4, longs.size());
    }

// org.apache.commons.collections.TestCollectionUtils::countMatches
    public void countMatches() {
        assertEquals(4, CollectionUtils.countMatches(iterableB, EQUALS_TWO));
        assertEquals(0, CollectionUtils.countMatches(iterableA, null));
        assertEquals(0, CollectionUtils.countMatches(null, EQUALS_TWO));
        assertEquals(0, CollectionUtils.countMatches(null, null));
    }

// org.apache.commons.collections.TestCollectionUtils::exists
    public void exists() {
        List<Integer> list = new ArrayList<Integer>();
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

// org.apache.commons.collections.TestCollectionUtils::select
    public void select() {
        List<Integer> list = new ArrayList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        
        Collection<Integer> output1 = CollectionUtils.select(list, EQUALS_TWO);
        Collection<Number> output2 = CollectionUtils.<Number>select(list, EQUALS_TWO);
        HashSet<Number> output3 = CollectionUtils.select(list, EQUALS_TWO, new HashSet<Number>());
        assertTrue(CollectionUtils.isEqualCollection(output1, output3));
        assertEquals(4, list.size());
        assertEquals(1, output1.size());
        assertEquals(2, output2.iterator().next());
    }

// org.apache.commons.collections.TestCollectionUtils::selectRejected
    public void selectRejected() {
        List<Long> list = new ArrayList<Long>();
        list.add(1L);
        list.add(2L);
        list.add(3L);
        list.add(4L);
        Collection<Long> output1 = CollectionUtils.selectRejected(list, EQUALS_TWO);
        Collection<? extends Number> output2 = CollectionUtils.selectRejected(list, EQUALS_TWO);
        HashSet<Number> output3 = CollectionUtils.selectRejected(list, EQUALS_TWO, new HashSet<Number>());
        assertTrue(CollectionUtils.isEqualCollection(output1, output2));
        assertTrue(CollectionUtils.isEqualCollection(output1, output3));
        assertEquals(4, list.size());
        assertEquals(3, output1.size());
        assertTrue(output1.contains(1L));
        assertTrue(output1.contains(3L));
        assertTrue(output1.contains(4L));
    }

// org.apache.commons.collections.TestCollectionUtils::collect
    public void collect() {
        Transformer<Number, Long> transformer = TransformerUtils.constantTransformer(2L);
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

        int size = collectionA.size();
        collectionB = CollectionUtils.collect((Collection<Integer>) null, transformer, collectionB);
        assertTrue(collectionA.size() == size && collectionA.contains(1));
        CollectionUtils.collect(collectionB, null, collectionA);
        assertTrue(collectionA.size() == size && collectionA.contains(1));

    }

// org.apache.commons.collections.TestCollectionUtils::transform1
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

// org.apache.commons.collections.TestCollectionUtils::transform2
    public void transform2() {
        Set<Number> set = new HashSet<Number>();
        set.add(1L);
        set.add(2L);
        set.add(3L);
        CollectionUtils.transform(set, new Transformer<Object, Integer>() {
            public Integer transform(Object input) {
                return 4;
            }
        });
        assertEquals(1, set.size());
        assertEquals(4, set.iterator().next());
    }

// org.apache.commons.collections.TestCollectionUtils::addIgnoreNull
    public void addIgnoreNull() {
        Set<String> set = new HashSet<String>();
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

// org.apache.commons.collections.TestCollectionUtils::predicatedCollection
    public void predicatedCollection() {
        Predicate<Object> predicate = PredicateUtils.instanceofPredicate(Integer.class);
        Collection<Number> collection = CollectionUtils.predicatedCollection(new ArrayList<Number>(), predicate);
        assertTrue("returned object should be a PredicatedCollection", collection instanceof PredicatedCollection);
        try {
            collection = CollectionUtils.predicatedCollection(new ArrayList<Number>(), null);
            fail("Expecting IllegalArgumentException for null predicate.");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            CollectionUtils.predicatedCollection(null, predicate);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections.TestCollectionUtils::isFull
    public void isFull() {
        Set<String> set = new HashSet<String>();
        set.add("1");
        set.add("2");
        set.add("3");
        try {
            CollectionUtils.isFull(null);
            fail();
        } catch (NullPointerException ex) {
        }
        assertFalse(CollectionUtils.isFull(set));

        BoundedFifoBuffer<String> buf = new BoundedFifoBuffer<String>(set);
        assertEquals(true, CollectionUtils.isFull(buf));
        buf.remove("2");
        assertFalse(CollectionUtils.isFull(buf));
        buf.add("2");
        assertEquals(true, CollectionUtils.isFull(buf));

        Buffer<String> buf2 = BufferUtils.synchronizedBuffer(buf);
        assertEquals(true, CollectionUtils.isFull(buf2));
        buf2.remove("2");
        assertFalse(CollectionUtils.isFull(buf2));
        buf2.add("2");
        assertEquals(true, CollectionUtils.isFull(buf2));
    }

// org.apache.commons.collections.TestCollectionUtils::isEmpty
    public void isEmpty() {
        assertFalse(CollectionUtils.isNotEmpty(null));
        assertTrue(CollectionUtils.isNotEmpty(collectionA));
    }

// org.apache.commons.collections.TestCollectionUtils::maxSize
    public void maxSize() {
        Set<String> set = new HashSet<String>();
        set.add("1");
        set.add("2");
        set.add("3");
        try {
            CollectionUtils.maxSize(null);
            fail();
        } catch (NullPointerException ex) {
        }
        assertEquals(-1, CollectionUtils.maxSize(set));

        Buffer<String> buf = new BoundedFifoBuffer<String>(set);
        assertEquals(3, CollectionUtils.maxSize(buf));
        buf.remove("2");
        assertEquals(3, CollectionUtils.maxSize(buf));
        buf.add("2");
        assertEquals(3, CollectionUtils.maxSize(buf));

        Buffer<String> buf2 = BufferUtils.synchronizedBuffer(buf);
        assertEquals(3, CollectionUtils.maxSize(buf2));
        buf2.remove("2");
        assertEquals(3, CollectionUtils.maxSize(buf2));
        buf2.add("2");
        assertEquals(3, CollectionUtils.maxSize(buf2));
    }

// org.apache.commons.collections.TestCollectionUtils::intersectionUsesMethodEquals
    public void intersectionUsesMethodEquals() {
        
        Integer elta = new Integer(17);
        Integer eltb = new Integer(17);

        
        assertEquals(elta, eltb);
        assertEquals(eltb, elta);

        
        assertTrue(elta != eltb);

        
        Collection<Number> cola = new ArrayList<Number>();
        Collection<Integer> colb = new ArrayList<Integer>();

        
        
        cola.add(elta);
        colb.add(eltb);

        
        
        Collection<Number> intersection = CollectionUtils.intersection(cola, colb);
        assertEquals(1, intersection.size());

        
        
        
        Object eltc = intersection.iterator().next();
        assertTrue((eltc == elta && eltc != eltb) || (eltc != elta && eltc == eltb));

        
        
        assertEquals(elta, eltc);
        assertEquals(eltc, elta);
        assertEquals(eltb, eltc);
        assertEquals(eltc, eltb);
    }

// org.apache.commons.collections.TestCollectionUtils::testRetainAll
    public void testRetainAll() {
        List<String> base = new ArrayList<String>();
        base.add("A");
        base.add("B");
        base.add("C");
        List<Object> sub = new ArrayList<Object>();
        sub.add("A");
        sub.add("C");
        sub.add("X");

        Collection<String> result = CollectionUtils.retainAll(base, sub);
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
        } catch (NullPointerException npe) {
        } 
    }

// org.apache.commons.collections.TestCollectionUtils::testRemoveAll
    public void testRemoveAll() {
        List<String> base = new ArrayList<String>();
        base.add("A");
        base.add("B");
        base.add("C");
        List<String> sub = new ArrayList<String>();
        sub.add("A");
        sub.add("C");
        sub.add("X");

        Collection<String> result = CollectionUtils.removeAll(base, sub);
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
        } catch (NullPointerException npe) {
        } 
    }

// org.apache.commons.collections.TestCollectionUtils::testTransformedCollection
    public void testTransformedCollection() {
        Transformer<Object, Object> transformer = TransformerUtils.nopTransformer();
        Collection<Object> collection = CollectionUtils.transformingCollection(new ArrayList<Object>(), transformer);
        assertTrue("returned object should be a TransformedCollection", collection instanceof TransformedCollection);
        try {
            collection = CollectionUtils.transformingCollection(new ArrayList<Object>(), null);
            fail("Expecting IllegalArgumentException for null transformer.");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            collection = CollectionUtils.transformingCollection(null, transformer);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections.TestCollectionUtils::testTransformedCollection_2
    public void testTransformedCollection_2() {
        List<Object> list = new ArrayList<Object>();
        list.add("1");
        list.add("2");
        list.add("3");
        Collection<Object> result = CollectionUtils.transformingCollection(list, TRANSFORM_TO_INTEGER);
        assertEquals(true, result.contains("1")); 
        assertEquals(true, result.contains("2")); 
        assertEquals(true, result.contains("3")); 
    }

// org.apache.commons.collections.TestCollectionUtils::testSynchronizedCollection
    public void testSynchronizedCollection() {
        Collection<Object> col = CollectionUtils.synchronizedCollection(new ArrayList<Object>());
        assertTrue("Returned object should be a SynchronizedCollection.", col instanceof SynchronizedCollection);
        try {
            col = CollectionUtils.synchronizedCollection(null);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections.TestCollectionUtils::testUnmodifiableCollection
    public void testUnmodifiableCollection() {
        Collection<Object> col = CollectionUtils.unmodifiableCollection(new ArrayList<Object>());
        assertTrue("Returned object should be a UnmodifiableCollection.", col instanceof UnmodifiableCollection);
        try {
            col = CollectionUtils.unmodifiableCollection(null);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections.TestCollectionUtils::emptyCollection
    public void emptyCollection() throws Exception {
        Collection<Number> coll = CollectionUtils.emptyCollection();
        assertEquals(CollectionUtils.EMPTY_COLLECTION, coll);
    }

// org.apache.commons.collections.TestCollectionUtils::addAllForIterable
    public void addAllForIterable() {
        Collection<Integer> inputCollection = createMock(Collection.class);
        Iterable<Integer> inputIterable = inputCollection;
        Iterable<Long> iterable = createMock(Iterable.class);
        Iterator<Long> iterator = createMock(Iterator.class);
        Collection<Number> c = createMock(Collection.class);

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

// org.apache.commons.collections.TestCollectionUtils::addAllForEnumeration
    public void addAllForEnumeration() {
        Hashtable<Integer, Integer> h = new Hashtable<Integer, Integer>();
        h.put(5, 5);
        Enumeration<? extends Integer> enumeration = h.keys();
        CollectionUtils.addAll(collectionA, enumeration);
        assertTrue(collectionA.contains(5));
    }

// org.apache.commons.collections.TestCollectionUtils::addAllForElements
    public void addAllForElements() {
        CollectionUtils.addAll(collectionA, new Integer[]{5});
        assertTrue(collectionA.contains(5));
    }

// org.apache.commons.collections.TestCollectionUtils::getNegative
    public void getNegative() {
        CollectionUtils.get((Object)collectionA, -3);
    }

// org.apache.commons.collections.TestCollectionUtils::getPositiveOutOfBounds
    public void getPositiveOutOfBounds() {
        CollectionUtils.get((Object)collectionA.iterator(), 30);
    }

// org.apache.commons.collections.TestCollectionUtils::get1
    public void get1() {
        CollectionUtils.get((Object)null, 0);
    }

// org.apache.commons.collections.TestCollectionUtils::get
    public void get() {
        assertEquals(2, CollectionUtils.get((Object)collectionA, 2));
        assertEquals(2, CollectionUtils.get((Object)collectionA.iterator(), 2));
        Map<Integer, Integer> map = CollectionUtils.getCardinalityMap(collectionA);
        assertEquals(map.entrySet().iterator().next(), CollectionUtils.get(
                (Object)map, 0));
    }

// org.apache.commons.collections.TestCollectionUtils::ensureCollectionUtilsCanBeExtended
    public void ensureCollectionUtilsCanBeExtended() {
        new CollectionUtils() {};
    }

// org.apache.commons.collections.TestCollectionUtils::reverse
    public void reverse() {
        CollectionUtils.reverseArray(new Object[] {});
        Integer[] a = collectionA.toArray(new Integer[collectionA.size()]);
        CollectionUtils.reverseArray(a);
        
        Collections.reverse(collectionA);
        assertEquals(collectionA, Arrays.asList(a));
    }

// org.apache.commons.collections.TestCollectionUtils::extractSingleton
    public void extractSingleton() {
        ArrayList<String> coll = null;
        try {
            CollectionUtils.extractSingleton(coll);
            fail("expected IllegalArgumentException from extractSingleton(null)");
        } catch (IllegalArgumentException e) {
        }
        coll = new ArrayList<String>();
        try {
            CollectionUtils.extractSingleton(coll);
            fail("expected IllegalArgumentException from extractSingleton(empty)");
        } catch (IllegalArgumentException e) {
        }
        coll.add("foo");
        assertEquals("foo", CollectionUtils.extractSingleton(coll));
        coll.add("bar");
        try {
            CollectionUtils.extractSingleton(coll);
            fail("expected IllegalArgumentException from extractSingleton(size == 2)");
        } catch (IllegalArgumentException e) {
        }
    }

// org.apache.commons.collections.TestListUtils::testNothing
    public void testNothing() {
    }

// org.apache.commons.collections.TestListUtils::testIntersectNonEmptyWithEmptyList
    public void testIntersectNonEmptyWithEmptyList() {
        final List<String> empty = Collections.<String>emptyList();
        assertTrue("result not empty", ListUtils.intersection(empty, fullList).isEmpty());
    }

// org.apache.commons.collections.TestListUtils::testIntersectEmptyWithEmptyList
    public void testIntersectEmptyWithEmptyList() {
        final List<?> empty = Collections.EMPTY_LIST;
        assertTrue("result not empty", ListUtils.intersection(empty, empty).isEmpty());
    }

// org.apache.commons.collections.TestListUtils::testIntersectNonEmptySubset
    public void testIntersectNonEmptySubset() {
        
        final List<String> other = new ArrayList<String>(fullList);

        
        assertNotNull(other.remove(0));
        assertNotNull(other.remove(1));

        
        assertEquals(other, ListUtils.intersection(fullList, other));
    }

// org.apache.commons.collections.TestListUtils::testIntersectListWithNoOverlapAndDifferentTypes
    public void testIntersectListWithNoOverlapAndDifferentTypes() {
        @SuppressWarnings("boxing")
        final List<Integer> other = Arrays.asList(1, 23);
        assertTrue(ListUtils.intersection(fullList, other).isEmpty());
    }

// org.apache.commons.collections.TestListUtils::testIntersectListWithSelf
    public void testIntersectListWithSelf() {
        assertEquals(fullList, ListUtils.intersection(fullList, fullList));
    }

// org.apache.commons.collections.TestListUtils::testIntersectionOrderInsensitivity
    public void testIntersectionOrderInsensitivity() {
        List<String> one = new ArrayList<String>();
        List<String> two = new ArrayList<String>();
        one.add("a");
        one.add("b");
        two.add("a");
        two.add("a");
        two.add("b");
        two.add("b");
        assertEquals(ListUtils.intersection(one,two),ListUtils.intersection(two, one));
    }

// org.apache.commons.collections.TestListUtils::testPredicatedList
    public void testPredicatedList() {
        Predicate<Object> predicate = new Predicate<Object>() {
            public boolean evaluate(Object o) {
                return o instanceof String;
            }
        };
        List<Object> list = ListUtils.predicatedList(new ArrayStack<Object>(), predicate);
        assertTrue("returned object should be a PredicatedList", list instanceof PredicatedList);
        try {
            list = ListUtils.predicatedList(new ArrayStack<Object>(), null);
            fail("Expecting IllegalArgumentException for null predicate.");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            list = ListUtils.predicatedList(null, predicate);
            fail("Expecting IllegalArgumentException for null list.");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections.TestListUtils::testLazyList
    public void testLazyList() {
        List<Integer> list = ListUtils.lazyList(new ArrayList<Integer>(), new Factory<Integer>() {

            private int index;

            public Integer create() {
                index++;
                return new Integer(index);
            }
        });

        assertNotNull(list.get(5));
        assertEquals(6, list.size());

        assertNotNull(list.get(5));
        assertEquals(6, list.size());
    }

// org.apache.commons.collections.TestListUtils::testEquals
    public void testEquals() {
        Collection<String> data = Arrays.asList( new String[] { "a", "b", "c" });

        List<String> a = new ArrayList<String>( data );
        List<String> b = new ArrayList<String>( data );

        assertEquals(true, a.equals(b));
        assertEquals(true, ListUtils.isEqualList(a, b));
        a.clear();
        assertEquals(false, ListUtils.isEqualList(a, b));
        assertEquals(false, ListUtils.isEqualList(a, null));
        assertEquals(false, ListUtils.isEqualList(null, b));
        assertEquals(true, ListUtils.isEqualList(null, null));
    }

// org.apache.commons.collections.TestListUtils::testHashCode
    public void testHashCode() {
        Collection<String> data = Arrays.asList( new String[] { "a", "b", "c" });

        List<String> a = new ArrayList<String>(data);
        List<String> b = new ArrayList<String>(data);

        assertEquals(true, a.hashCode() == b.hashCode());
        assertEquals(true, a.hashCode() == ListUtils.hashCodeForList(a));
        assertEquals(true, b.hashCode() == ListUtils.hashCodeForList(b));
        assertEquals(true, ListUtils.hashCodeForList(a) == ListUtils.hashCodeForList(b));
        a.clear();
        assertEquals(false, ListUtils.hashCodeForList(a) == ListUtils.hashCodeForList(b));
        assertEquals(0, ListUtils.hashCodeForList(null));
    }

// org.apache.commons.collections.TestListUtils::testRetainAll
    public void testRetainAll() {
        List<String> sub = new ArrayList<String>();
        sub.add(a);
        sub.add(b);
        sub.add(x);

        List<String> retained = ListUtils.retainAll(fullList, sub);
        assertTrue(retained.size() == 2);
        sub.remove(x);
        assertTrue(retained.equals(sub));
        fullList.retainAll(sub);
        assertTrue(retained.equals(fullList));

        try {
            ListUtils.retainAll(null, null);
            fail("expecting NullPointerException");
        } catch(NullPointerException npe){} 
    }

// org.apache.commons.collections.TestListUtils::testRemoveAll
    public void testRemoveAll() {
        List<String> sub = new ArrayList<String>();
        sub.add(a);
        sub.add(b);
        sub.add(x);

        List<String> remainder = ListUtils.removeAll(fullList, sub);
        assertTrue(remainder.size() == 3);
        fullList.removeAll(sub);
        assertTrue(remainder.equals(fullList));

        try {
            ListUtils.removeAll(null, null);
            fail("expecting NullPointerException");
        } catch(NullPointerException npe) {} 
    }

// org.apache.commons.collections.TestListUtils::testSubtract
    public void testSubtract() {
        List<String> list = new ArrayList<String>();
        list.add(a);
        list.add(b);
        list.add(a);
        list.add(x);

        List<String> sub = new ArrayList<String>();
        sub.add(a);

        List<String> result = ListUtils.subtract(list, sub);
        assertTrue(result.size() == 3);
        
        List<String> expected = new ArrayList<String>();
        expected.add(b);
        expected.add(a);
        expected.add(x);

        assertEquals(expected, result);
        
        try {
            ListUtils.subtract(list, null);
            fail("expecting NullPointerException");
        } catch(NullPointerException npe) {} 
    }

// org.apache.commons.collections.TestListUtils::testSubtractNullElement
    public void testSubtractNullElement() {
        List<String> list = new ArrayList<String>();
        list.add(a);
        list.add(null);
        list.add(null);
        list.add(x);

        List<String> sub = new ArrayList<String>();
        sub.add(null);

        List<String> result = ListUtils.subtract(list, sub);
        assertTrue(result.size() == 3);
        
        List<String> expected = new ArrayList<String>();
        expected.add(a);
        expected.add(null);
        expected.add(x);

        assertEquals(expected, result);
    }

// org.apache.commons.collections.TestListUtils::testIndexOf
    public void testIndexOf() {
        Predicate<String> testPredicate = EqualPredicate.equalPredicate("d");
        int index = ListUtils.indexOf(fullList, testPredicate);
        assertEquals(d, fullList.get(index));

        testPredicate = EqualPredicate.equalPredicate("de");
        index = ListUtils.indexOf(fullList, testPredicate);
        assertEquals(index, -1);
        
        assertEquals(ListUtils.indexOf(null,testPredicate), -1);
        assertEquals(ListUtils.indexOf(fullList, null), -1);
    }

// org.apache.commons.collections.TestPredicateUtils::testExceptionPredicate
    @Test public void testExceptionPredicate() {
        assertNotNull(PredicateUtils.exceptionPredicate());
        assertSame(PredicateUtils.exceptionPredicate(), PredicateUtils.exceptionPredicate());
        try {
            PredicateUtils.exceptionPredicate().evaluate(null);
        } catch (FunctorException ex) {
            try {
                PredicateUtils.exceptionPredicate().evaluate(cString);
            } catch (FunctorException ex2) {
                return;
            }
        }
        fail();
    }

// org.apache.commons.collections.TestPredicateUtils::testIsNotNullPredicate
    @Test public void testIsNotNullPredicate() {
        assertNotNull(PredicateUtils.notNullPredicate());
        assertSame(PredicateUtils.notNullPredicate(), PredicateUtils.notNullPredicate());
        assertEquals(false, PredicateUtils.notNullPredicate().evaluate(null));
        assertEquals(true, PredicateUtils.notNullPredicate().evaluate(cObject));
        assertEquals(true, PredicateUtils.notNullPredicate().evaluate(cString));
        assertEquals(true, PredicateUtils.notNullPredicate().evaluate(cInteger));
    }

// org.apache.commons.collections.TestPredicateUtils::testIdentityPredicate
    @Test public void testIdentityPredicate() {
        assertSame(nullPredicate(), PredicateUtils.identityPredicate(null));
        assertNotNull(PredicateUtils.identityPredicate(new Integer(6)));
        assertEquals(false, PredicateUtils.identityPredicate(new Integer(6)).evaluate(null));
        assertEquals(false, PredicateUtils.<Object>identityPredicate(new Integer(6)).evaluate(cObject));
        assertEquals(false, PredicateUtils.<Object>identityPredicate(new Integer(6)).evaluate(cString));
        assertEquals(false, PredicateUtils.identityPredicate(new Integer(6)).evaluate(cInteger));
        assertEquals(true, PredicateUtils.identityPredicate(cInteger).evaluate(cInteger));
    }

// org.apache.commons.collections.TestPredicateUtils::testTruePredicate
    @Test public void testTruePredicate() {
        assertNotNull(TruePredicate.truePredicate());
        assertSame(TruePredicate.truePredicate(), TruePredicate.truePredicate());
        assertEquals(true, TruePredicate.truePredicate().evaluate(null));
        assertEquals(true, TruePredicate.truePredicate().evaluate(cObject));
        assertEquals(true, TruePredicate.truePredicate().evaluate(cString));
        assertEquals(true, TruePredicate.truePredicate().evaluate(cInteger));
    }

// org.apache.commons.collections.TestPredicateUtils::testFalsePredicate
    @Test public void testFalsePredicate() {
        assertNotNull(FalsePredicate.falsePredicate());
        assertSame(FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate());
        assertEquals(false, FalsePredicate.falsePredicate().evaluate(null));
        assertEquals(false, FalsePredicate.falsePredicate().evaluate(cObject));
        assertEquals(false, FalsePredicate.falsePredicate().evaluate(cString));
        assertEquals(false, FalsePredicate.falsePredicate().evaluate(cInteger));
    }

// org.apache.commons.collections.TestPredicateUtils::testNotPredicate
    @Test public void testNotPredicate() {
        assertNotNull(PredicateUtils.notPredicate(TruePredicate.truePredicate()));
        assertEquals(false, PredicateUtils.notPredicate(TruePredicate.truePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.notPredicate(TruePredicate.truePredicate()).evaluate(cObject));
        assertEquals(false, PredicateUtils.notPredicate(TruePredicate.truePredicate()).evaluate(cString));
        assertEquals(false, PredicateUtils.notPredicate(TruePredicate.truePredicate()).evaluate(cInteger));
    }

// org.apache.commons.collections.TestPredicateUtils::testNotPredicateEx
    public void testNotPredicateEx() {
        PredicateUtils.notPredicate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAndPredicate
    @Test public void testAndPredicate() {
        assertEquals(true, PredicateUtils.andPredicate(TruePredicate.truePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.andPredicate(TruePredicate.truePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.andPredicate(FalsePredicate.falsePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.andPredicate(FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
    }

// org.apache.commons.collections.TestPredicateUtils::testAndPredicateEx
    public void testAndPredicateEx() {
        PredicateUtils.andPredicate(null, null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAllPredicate
    @Test public void testAllPredicate() {
        assertTrue(AllPredicate.allPredicate(new Predicate[] {}), null);
        assertEquals(true, AllPredicate.allPredicate(new Predicate[] {
                TruePredicate.truePredicate(), TruePredicate.truePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(false, AllPredicate.allPredicate(new Predicate[] {
                TruePredicate.truePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(false, AllPredicate.allPredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(false, AllPredicate.allPredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()}).evaluate(null));
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(true, AllPredicate.allPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(false, AllPredicate.allPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(false, AllPredicate.allPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        assertEquals(false, AllPredicate.allPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        assertFalse(AllPredicate.allPredicate(coll), null);
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        assertTrue(AllPredicate.allPredicate(coll), null);
        coll.clear();
        assertTrue(AllPredicate.allPredicate(coll), null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAllPredicateEx1
    public void testAllPredicateEx1() {
        AllPredicate.allPredicate((Predicate<Object>[]) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAllPredicateEx2
    public void testAllPredicateEx2() {
        AllPredicate.<Object>allPredicate(new Predicate[] { null });
    }

// org.apache.commons.collections.TestPredicateUtils::testAllPredicateEx3
    public void testAllPredicateEx3() {
        AllPredicate.allPredicate(new Predicate[] { null, null });
    }

// org.apache.commons.collections.TestPredicateUtils::testAllPredicateEx4
    public void testAllPredicateEx4() {
        AllPredicate.allPredicate((Collection<Predicate<Object>>) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAllPredicateEx5
    @Test public void testAllPredicateEx5() {
        AllPredicate.allPredicate(Collections.<Predicate<Object>>emptyList());
    }

// org.apache.commons.collections.TestPredicateUtils::testAllPredicateEx6
    public void testAllPredicateEx6() {
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(null);
        coll.add(null);
        AllPredicate.allPredicate(coll);
    }

// org.apache.commons.collections.TestPredicateUtils::testOrPredicate
    @Test public void testOrPredicate() {
        assertEquals(true, PredicateUtils.orPredicate(TruePredicate.truePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(true, PredicateUtils.orPredicate(TruePredicate.truePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
        assertEquals(true, PredicateUtils.orPredicate(FalsePredicate.falsePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.orPredicate(FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
    }

// org.apache.commons.collections.TestPredicateUtils::testOrPredicateEx
    public void testOrPredicateEx() {
        PredicateUtils.orPredicate(null, null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAnyPredicate
    @Test public void testAnyPredicate() {
        assertFalse(PredicateUtils.anyPredicate(new Predicate[] {}), null);

        assertEquals(true, PredicateUtils.anyPredicate(new Predicate[] {
                TruePredicate.truePredicate(), TruePredicate.truePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(true, PredicateUtils.anyPredicate(new Predicate[] {
                TruePredicate.truePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(true, PredicateUtils.anyPredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(false, PredicateUtils.anyPredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()}).evaluate(null));
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(true, PredicateUtils.anyPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(true, PredicateUtils.anyPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(true, PredicateUtils.anyPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        assertEquals(false, PredicateUtils.anyPredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        assertFalse(PredicateUtils.anyPredicate(coll), null);
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        assertTrue(PredicateUtils.anyPredicate(coll), null);
        coll.clear();
        assertFalse(PredicateUtils.anyPredicate(coll), null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAnyPredicateEx1
    public void testAnyPredicateEx1() {
        PredicateUtils.anyPredicate((Predicate<Object>[]) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAnyPredicateEx2
    public void testAnyPredicateEx2() {
        PredicateUtils.anyPredicate(new Predicate[] {null});
    }

// org.apache.commons.collections.TestPredicateUtils::testAnyPredicateEx3
    public void testAnyPredicateEx3() {
        PredicateUtils.anyPredicate(new Predicate[] {null, null});
    }

// org.apache.commons.collections.TestPredicateUtils::testAnyPredicateEx4
    public void testAnyPredicateEx4() {
        PredicateUtils.anyPredicate((Collection<Predicate<Object>>) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAnyPredicateEx5
    @Test public void testAnyPredicateEx5() {
        PredicateUtils.anyPredicate(Collections.<Predicate<Object>>emptyList());
    }

// org.apache.commons.collections.TestPredicateUtils::testAnyPredicateEx6
    public void testAnyPredicateEx6() {
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(null);
        coll.add(null);
        PredicateUtils.anyPredicate(coll);
    }

// org.apache.commons.collections.TestPredicateUtils::testEitherPredicate
    @Test public void testEitherPredicate() {
        assertEquals(false, PredicateUtils.eitherPredicate(TruePredicate.truePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(true, PredicateUtils.eitherPredicate(TruePredicate.truePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
        assertEquals(true, PredicateUtils.eitherPredicate(FalsePredicate.falsePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.eitherPredicate(FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
    }

// org.apache.commons.collections.TestPredicateUtils::testEitherPredicateEx
    public void testEitherPredicateEx() {
        PredicateUtils.eitherPredicate(null, null);
    }

// org.apache.commons.collections.TestPredicateUtils::testOnePredicate
    @Test public void testOnePredicate() {
        assertFalse(PredicateUtils.onePredicate((Predicate<Object>[]) new Predicate[] {}), null);
        assertEquals(false, PredicateUtils.onePredicate(new Predicate[] {
            TruePredicate.truePredicate(), TruePredicate.truePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(false, PredicateUtils.onePredicate(new Predicate[] {
                TruePredicate.truePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(true, PredicateUtils.onePredicate(new Predicate[] {
                TruePredicate.truePredicate(), FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()}).evaluate(null));
        assertEquals(true, PredicateUtils.onePredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), TruePredicate.truePredicate(), FalsePredicate.falsePredicate()}).evaluate(null));
        assertEquals(true, PredicateUtils.onePredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate()}).evaluate(null));
        assertEquals(false, PredicateUtils.onePredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()}).evaluate(null));
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(false, PredicateUtils.onePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(false, PredicateUtils.onePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(true, PredicateUtils.onePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        assertEquals(false, PredicateUtils.onePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        assertFalse(PredicateUtils.onePredicate(coll), null);
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        assertTrue(PredicateUtils.onePredicate(coll), null);
        coll.clear();
        assertFalse(PredicateUtils.onePredicate(coll), null);
    }

// org.apache.commons.collections.TestPredicateUtils::testOnePredicateEx1
    public void testOnePredicateEx1() {
        PredicateUtils.onePredicate((Predicate<Object>[]) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testOnePredicateEx2
    public void testOnePredicateEx2() {
        PredicateUtils.onePredicate(new Predicate[] {null});
    }

// org.apache.commons.collections.TestPredicateUtils::testOnePredicateEx3
    public void testOnePredicateEx3() {
        PredicateUtils.onePredicate(new Predicate[] {null, null});
    }

// org.apache.commons.collections.TestPredicateUtils::testOnePredicateEx4
    public void testOnePredicateEx4() {
        PredicateUtils.onePredicate((Collection<Predicate<Object>>) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testOnePredicateEx5
    @Test public void testOnePredicateEx5() {
        PredicateUtils.onePredicate(Collections.EMPTY_LIST);
    }

// org.apache.commons.collections.TestPredicateUtils::testOnePredicateEx6
    public void testOnePredicateEx6() {
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(null);
        coll.add(null);
        PredicateUtils.onePredicate(coll);
    }

// org.apache.commons.collections.TestPredicateUtils::testNeitherPredicate
    @Test public void testNeitherPredicate() {
        assertEquals(false, PredicateUtils.neitherPredicate(TruePredicate.truePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.neitherPredicate(TruePredicate.truePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
        assertEquals(false, PredicateUtils.neitherPredicate(FalsePredicate.falsePredicate(), TruePredicate.truePredicate()).evaluate(null));
        assertEquals(true, PredicateUtils.neitherPredicate(FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate()).evaluate(null));
    }

// org.apache.commons.collections.TestPredicateUtils::testNeitherPredicateEx
    public void testNeitherPredicateEx() {
        PredicateUtils.neitherPredicate(null, null);
    }

// org.apache.commons.collections.TestPredicateUtils::testNonePredicate
    @Test public void testNonePredicate() {
        assertTrue(PredicateUtils.nonePredicate(new Predicate[] {}), null);
        assertEquals(false, PredicateUtils.nonePredicate(new Predicate[] {
                TruePredicate.truePredicate(), TruePredicate.truePredicate(), TruePredicate.truePredicate() }).evaluate(null));
        assertEquals(false, PredicateUtils.nonePredicate(new Predicate[] {
                TruePredicate.truePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate() }).evaluate(null));
        assertEquals(false, PredicateUtils.nonePredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), TruePredicate.truePredicate() }).evaluate(null));
        assertEquals(true, PredicateUtils.nonePredicate(new Predicate[] {
                FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate(), FalsePredicate.falsePredicate() }).evaluate(null));
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(false, PredicateUtils.nonePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(false, PredicateUtils.nonePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(TruePredicate.truePredicate());
        assertEquals(false, PredicateUtils.nonePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        coll.add(FalsePredicate.falsePredicate());
        assertEquals(true, PredicateUtils.nonePredicate(coll).evaluate(null));
        coll.clear();
        coll.add(FalsePredicate.falsePredicate());
        assertTrue(PredicateUtils.nonePredicate(coll), null);
        coll.clear();
        coll.add(TruePredicate.truePredicate());
        assertFalse(PredicateUtils.nonePredicate(coll), null);
        coll.clear();
        assertTrue(PredicateUtils.nonePredicate(coll), null);
    }

// org.apache.commons.collections.TestPredicateUtils::testNonePredicateEx1
    public void testNonePredicateEx1() {
        PredicateUtils.nonePredicate((Predicate<Object>[]) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testNonePredicateEx2
    public void testNonePredicateEx2() {
        PredicateUtils.nonePredicate(new Predicate[] {null});
    }

// org.apache.commons.collections.TestPredicateUtils::testNonePredicateEx3
    public void testNonePredicateEx3() {
        PredicateUtils.nonePredicate(new Predicate[] {null, null});
    }

// org.apache.commons.collections.TestPredicateUtils::testNonePredicateEx4
    public void testNonePredicateEx4() {
        PredicateUtils.nonePredicate((Collection<Predicate<Object>>) null);
    }

// org.apache.commons.collections.TestPredicateUtils::testNonePredicateEx5
    @Test public void testNonePredicateEx5() {
        PredicateUtils.nonePredicate(Collections.<Predicate<Object>>emptyList());
    }

// org.apache.commons.collections.TestPredicateUtils::testNonePredicateEx6
    public void testNonePredicateEx6() {
        Collection<Predicate<Object>> coll = new ArrayList<Predicate<Object>>();
        coll.add(null);
        coll.add(null);
        PredicateUtils.nonePredicate(coll);
    }

// org.apache.commons.collections.TestPredicateUtils::testInstanceOfPredicate
    @Test public void testInstanceOfPredicate() {
        assertNotNull(PredicateUtils.instanceofPredicate(String.class));
        assertEquals(false, PredicateUtils.instanceofPredicate(String.class).evaluate(null));
        assertEquals(false, PredicateUtils.instanceofPredicate(String.class).evaluate(cObject));
        assertEquals(true, PredicateUtils.instanceofPredicate(String.class).evaluate(cString));
        assertEquals(false, PredicateUtils.instanceofPredicate(String.class).evaluate(cInteger));
    }

// org.apache.commons.collections.TestPredicateUtils::testUniquePredicate
    @Test public void testUniquePredicate() {
        Predicate<Object> p = PredicateUtils.uniquePredicate();
        assertEquals(true, p.evaluate(new Object()));
        assertEquals(true, p.evaluate(new Object()));
        assertEquals(true, p.evaluate(new Object()));
        assertEquals(true, p.evaluate(cString));
        assertEquals(false, p.evaluate(cString));
        assertEquals(false, p.evaluate(cString));
    }

// org.apache.commons.collections.TestPredicateUtils::testAsPredicateTransformer
    @Test public void testAsPredicateTransformer() {
        assertEquals(false, PredicateUtils.asPredicate(TransformerUtils.<Boolean>nopTransformer()).evaluate(false));
        assertEquals(true, PredicateUtils.asPredicate(TransformerUtils.<Boolean>nopTransformer()).evaluate(true));
    }

// org.apache.commons.collections.TestPredicateUtils::testAsPredicateTransformerEx1
    public void testAsPredicateTransformerEx1() {
        PredicateUtils.asPredicate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testAsPredicateTransformerEx2
    public void testAsPredicateTransformerEx2() {
        PredicateUtils.asPredicate(TransformerUtils.<Boolean>nopTransformer()).evaluate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicate
    @Test public void testInvokerPredicate() {
        List<Object> list = new ArrayList<Object>();
        assertEquals(true, PredicateUtils.invokerPredicate("isEmpty").evaluate(list));
        list.add(new Object());
        assertEquals(false, PredicateUtils.invokerPredicate("isEmpty").evaluate(list));
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicateEx1
    public void testInvokerPredicateEx1() {
        PredicateUtils.invokerPredicate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicateEx2
    public void testInvokerPredicateEx2() {
        PredicateUtils.invokerPredicate("isEmpty").evaluate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicateEx3
    public void testInvokerPredicateEx3() {
        PredicateUtils.invokerPredicate("noSuchMethod").evaluate(new Object());
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicate2
    @Test public void testInvokerPredicate2() {
        List<String> list = new ArrayList<String>();
        assertEquals(false, PredicateUtils.invokerPredicate(
            "contains", new Class[] {Object.class}, new Object[] {cString}).evaluate(list));
        list.add(cString);
        assertEquals(true, PredicateUtils.invokerPredicate(
            "contains", new Class[] {Object.class}, new Object[] {cString}).evaluate(list));
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicate2Ex1
    public void testInvokerPredicate2Ex1() {
        PredicateUtils.invokerPredicate(null, null, null);
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicate2Ex2
    public void testInvokerPredicate2Ex2() {
        PredicateUtils.invokerPredicate("contains", new Class[] {Object.class}, new Object[] {cString}).evaluate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testInvokerPredicate2Ex3
    public void testInvokerPredicate2Ex3() {
        PredicateUtils.invokerPredicate(
                "noSuchMethod", new Class[] {Object.class}, new Object[] {cString}).evaluate(new Object());
    }

// org.apache.commons.collections.TestPredicateUtils::testNullIsExceptionPredicate
    public void testNullIsExceptionPredicate() {
        assertEquals(true, PredicateUtils.nullIsExceptionPredicate(TruePredicate.truePredicate()).evaluate(new Object()));
        PredicateUtils.nullIsExceptionPredicate(TruePredicate.truePredicate()).evaluate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testNullIsExceptionPredicateEx1
    public void testNullIsExceptionPredicateEx1() {
        PredicateUtils.nullIsExceptionPredicate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testNullIsTruePredicate
    @Test public void testNullIsTruePredicate() {
        assertEquals(true, PredicateUtils.nullIsTruePredicate(TruePredicate.truePredicate()).evaluate(null));
        assertEquals(true, PredicateUtils.nullIsTruePredicate(TruePredicate.truePredicate()).evaluate(new Object()));
        assertEquals(false, PredicateUtils.nullIsTruePredicate(FalsePredicate.falsePredicate()).evaluate(new Object()));
    }

// org.apache.commons.collections.TestPredicateUtils::testNullIsTruePredicateEx1
    public void testNullIsTruePredicateEx1() {
        PredicateUtils.nullIsTruePredicate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testNullIsFalsePredicate
    @Test public void testNullIsFalsePredicate() {
        assertEquals(false, PredicateUtils.nullIsFalsePredicate(TruePredicate.truePredicate()).evaluate(null));
        assertEquals(true, PredicateUtils.nullIsFalsePredicate(TruePredicate.truePredicate()).evaluate(new Object()));
        assertEquals(false, PredicateUtils.nullIsFalsePredicate(FalsePredicate.falsePredicate()).evaluate(new Object()));
    }

// org.apache.commons.collections.TestPredicateUtils::testNullIsFalsePredicateEx1
    public void testNullIsFalsePredicateEx1() {
        PredicateUtils.nullIsFalsePredicate(null);
    }

// org.apache.commons.collections.TestPredicateUtils::testTransformedPredicate
    @Test public void testTransformedPredicate() {
        assertEquals(true, PredicateUtils.transformedPredicate(
                TransformerUtils.nopTransformer(),
                TruePredicate.truePredicate()).evaluate(new Object()));

        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put(Boolean.TRUE, "Hello");
        Transformer<Object, Object> t = TransformerUtils.mapTransformer(map);
        Predicate<Object> p = EqualPredicate.<Object>equalPredicate("Hello");
        assertEquals(false, PredicateUtils.transformedPredicate(t, p).evaluate(null));
        assertEquals(true, PredicateUtils.transformedPredicate(t, p).evaluate(Boolean.TRUE));
        try {
            PredicateUtils.transformedPredicate(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestPredicateUtils::testSingletonPatternInSerialization
    @Test public void testSingletonPatternInSerialization() {
        final Object[] singletones = new Object[] {
                ExceptionPredicate.INSTANCE,
                FalsePredicate.INSTANCE,
                NotNullPredicate.INSTANCE,
                NullPredicate.INSTANCE,
                TruePredicate.INSTANCE
        };

        for (final Object original : singletones) {
            TestUtils.assertSameAfterSerialization(
                    "Singletone patern broken for " + original.getClass(),
                    original
            );
        }
    }

// org.apache.commons.collections.TestTransformerUtils::testExceptionTransformer
    public void testExceptionTransformer() {
        assertNotNull(TransformerUtils.exceptionTransformer());
        assertSame(TransformerUtils.exceptionTransformer(), TransformerUtils.exceptionTransformer());
        try {
            TransformerUtils.exceptionTransformer().transform(null);
        } catch (FunctorException ex) {
            try {
                TransformerUtils.exceptionTransformer().transform(cString);
            } catch (FunctorException ex2) {
                return;
            }
        }
        fail();
    }

// org.apache.commons.collections.TestTransformerUtils::testNullTransformer
    public void testNullTransformer() {
        assertNotNull(TransformerUtils.nullTransformer());
        assertSame(TransformerUtils.nullTransformer(), TransformerUtils.nullTransformer());
        assertEquals(null, TransformerUtils.nullTransformer().transform(null));
        assertEquals(null, TransformerUtils.nullTransformer().transform(cObject));
        assertEquals(null, TransformerUtils.nullTransformer().transform(cString));
        assertEquals(null, TransformerUtils.nullTransformer().transform(cInteger));
    }

// org.apache.commons.collections.TestTransformerUtils::testNopTransformer
    public void testNopTransformer() {
        assertNotNull(TransformerUtils.nullTransformer());
        assertSame(TransformerUtils.nullTransformer(), TransformerUtils.nullTransformer());
        assertEquals(null, TransformerUtils.nopTransformer().transform(null));
        assertEquals(cObject, TransformerUtils.nopTransformer().transform(cObject));
        assertEquals(cString, TransformerUtils.nopTransformer().transform(cString));
        assertEquals(cInteger, TransformerUtils.nopTransformer().transform(cInteger));
    }

// org.apache.commons.collections.TestTransformerUtils::testConstantTransformer
    public void testConstantTransformer() {
        assertEquals(cObject, TransformerUtils.constantTransformer(cObject).transform(null));
        assertEquals(cObject, TransformerUtils.constantTransformer(cObject).transform(cObject));
        assertEquals(cObject, TransformerUtils.constantTransformer(cObject).transform(cString));
        assertEquals(cObject, TransformerUtils.constantTransformer(cObject).transform(cInteger));
        assertSame(ConstantTransformer.NULL_INSTANCE, TransformerUtils.constantTransformer(null));
    }

// org.apache.commons.collections.TestTransformerUtils::testCloneTransformer
    public void testCloneTransformer() {
        assertEquals(null, TransformerUtils.cloneTransformer().transform(null));
        assertEquals(cString, TransformerUtils.cloneTransformer().transform(cString));
        assertEquals(cInteger, TransformerUtils.cloneTransformer().transform(cInteger));
        try {
            assertEquals(cObject, TransformerUtils.cloneTransformer().transform(cObject));
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.collections.TestTransformerUtils::testMapTransformer
    public void testMapTransformer() {
        Map<Object, Integer> map = new HashMap<Object, Integer>();
        map.put(null, 0);
        map.put(cObject, 1);
        map.put(cString, 2);
        assertEquals(new Integer(0), TransformerUtils.mapTransformer(map).transform(null));
        assertEquals(new Integer(1), TransformerUtils.mapTransformer(map).transform(cObject));
        assertEquals(new Integer(2), TransformerUtils.mapTransformer(map).transform(cString));
        assertEquals(null, TransformerUtils.mapTransformer(map).transform(cInteger));
        assertSame(ConstantTransformer.NULL_INSTANCE, TransformerUtils.mapTransformer(null));
    }

// org.apache.commons.collections.TestTransformerUtils::testExecutorTransformer
    public void testExecutorTransformer() {
        assertEquals(null, TransformerUtils.asTransformer(ClosureUtils.nopClosure()).transform(null));
        assertEquals(cObject, TransformerUtils.asTransformer(ClosureUtils.nopClosure()).transform(cObject));
        assertEquals(cString, TransformerUtils.asTransformer(ClosureUtils.nopClosure()).transform(cString));
        assertEquals(cInteger, TransformerUtils.asTransformer(ClosureUtils.nopClosure()).transform(cInteger));
        try {
            TransformerUtils.asTransformer((Closure<Object>) null);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.collections.TestTransformerUtils::testPredicateTransformer
    public void testPredicateTransformer() {
        assertEquals(Boolean.TRUE, TransformerUtils.asTransformer(TruePredicate.truePredicate()).transform(null));
        assertEquals(Boolean.TRUE, TransformerUtils.asTransformer(TruePredicate.truePredicate()).transform(cObject));
        assertEquals(Boolean.TRUE, TransformerUtils.asTransformer(TruePredicate.truePredicate()).transform(cString));
        assertEquals(Boolean.TRUE, TransformerUtils.asTransformer(TruePredicate.truePredicate()).transform(cInteger));
        try {
            TransformerUtils.asTransformer((Predicate<Object>) null);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.collections.TestTransformerUtils::testFactoryTransformer
    public void testFactoryTransformer() {
        assertEquals(null, TransformerUtils.asTransformer(FactoryUtils.nullFactory()).transform(null));
        assertEquals(null, TransformerUtils.asTransformer(FactoryUtils.nullFactory()).transform(cObject));
        assertEquals(null, TransformerUtils.asTransformer(FactoryUtils.nullFactory()).transform(cString));
        assertEquals(null, TransformerUtils.asTransformer(FactoryUtils.nullFactory()).transform(cInteger));
        try {
            TransformerUtils.asTransformer((Factory<Object>) null);
        } catch (IllegalArgumentException ex) {
            return;
        }
        fail();
    }

// org.apache.commons.collections.TestTransformerUtils::testChainedTransformer
    public void testChainedTransformer() {
        Transformer<Object, Object> a = TransformerUtils.<Object, Object>constantTransformer("A");
        Transformer<Object, Object> b = TransformerUtils.constantTransformer((Object) "B");

        assertEquals("A", TransformerUtils.chainedTransformer(b, a).transform(null));
        assertEquals("B", TransformerUtils.chainedTransformer(a, b).transform(null));
        assertEquals("A", TransformerUtils.chainedTransformer(new Transformer[] { b, a }).transform(null));
        Collection<Transformer<Object, Object>> coll = new ArrayList<Transformer<Object, Object>>();
        coll.add(b);
        coll.add(a);
        assertEquals("A", TransformerUtils.chainedTransformer(coll).transform(null));

        assertSame(NOPTransformer.INSTANCE, TransformerUtils.chainedTransformer(new Transformer[0]));
        assertSame(NOPTransformer.INSTANCE, TransformerUtils.chainedTransformer(Collections.<Transformer<Object, Object>>emptyList()));

        try {
            TransformerUtils.chainedTransformer(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.chainedTransformer((Transformer[]) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.chainedTransformer((Collection<Transformer<Object, Object>>) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.chainedTransformer(new Transformer[] {null, null});
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            coll = new ArrayList<Transformer<Object, Object>>();
            coll.add(null);
            coll.add(null);
            TransformerUtils.chainedTransformer(coll);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestTransformerUtils::testSwitchTransformer
    public void testSwitchTransformer() {
        Transformer<String, String> a = TransformerUtils.constantTransformer("A");
        Transformer<String, String> b = TransformerUtils.constantTransformer("B");
        Transformer<String, String> c = TransformerUtils.constantTransformer("C");

        assertEquals("A", TransformerUtils.switchTransformer(TruePredicate.truePredicate(), a, b).transform(null));
        assertEquals("B", TransformerUtils.switchTransformer(FalsePredicate.falsePredicate(), a, b).transform(null));

        assertEquals(null, TransformerUtils.<Object, String>switchTransformer(
            new Predicate[] { EqualPredicate.equalPredicate("HELLO"), EqualPredicate.equalPredicate("THERE") },
            new Transformer[] { a, b }).transform("WELL"));
        assertEquals("A", TransformerUtils.switchTransformer(
            new Predicate[] { EqualPredicate.equalPredicate("HELLO"), EqualPredicate.equalPredicate("THERE") },
            new Transformer[] { a, b }).transform("HELLO"));
        assertEquals("B", TransformerUtils.switchTransformer(
            new Predicate[] { EqualPredicate.equalPredicate("HELLO"), EqualPredicate.equalPredicate("THERE") },
            new Transformer[] { a, b }).transform("THERE"));

        assertEquals("C", TransformerUtils.switchTransformer(
            new Predicate[] { EqualPredicate.equalPredicate("HELLO"), EqualPredicate.equalPredicate("THERE") },
            new Transformer[] { a, b }, c).transform("WELL"));

        Map<Predicate<String>, Transformer<String, String>> map = new HashMap<Predicate<String>, Transformer<String,String>>();
        map.put(EqualPredicate.equalPredicate("HELLO"), a);
        map.put(EqualPredicate.equalPredicate("THERE"), b);
        assertEquals(null, TransformerUtils.switchTransformer(map).transform("WELL"));
        assertEquals("A", TransformerUtils.switchTransformer(map).transform("HELLO"));
        assertEquals("B", TransformerUtils.switchTransformer(map).transform("THERE"));
        map.put(null, c);
        assertEquals("C", TransformerUtils.switchTransformer(map).transform("WELL"));

        assertEquals(ConstantTransformer.NULL_INSTANCE, TransformerUtils.switchTransformer(new Predicate[0], new Transformer[0]));
        assertEquals(ConstantTransformer.NULL_INSTANCE, TransformerUtils.switchTransformer(new HashMap<Predicate<Object>, Transformer<Object, Object>>()));
        map = new HashMap<Predicate<String>, Transformer<String, String>>();
        map.put(null, null);
        assertEquals(ConstantTransformer.NULL_INSTANCE, TransformerUtils.switchTransformer(map));

        try {
            TransformerUtils.switchTransformer(null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.switchTransformer((Predicate[]) null, (Transformer[]) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.switchTransformer((Map<Predicate<Object>, Transformer<Object, Object>>) null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.switchTransformer(new Predicate[2], new Transformer[2]);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.switchTransformer(
                    new Predicate[] { TruePredicate.truePredicate() },
                    new Transformer[] { a, b });
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestTransformerUtils::testSwitchMapTransformer
    public void testSwitchMapTransformer() {
        Transformer<String, String> a = TransformerUtils.constantTransformer("A");
        Transformer<String, String> b = TransformerUtils.constantTransformer("B");
        Transformer<String, String> c = TransformerUtils.constantTransformer("C");

        Map<String, Transformer<String, String>> map = new HashMap<String, Transformer<String,String>>();
        map.put("HELLO", a);
        map.put("THERE", b);
        assertEquals(null, TransformerUtils.switchMapTransformer(map).transform("WELL"));
        assertEquals("A", TransformerUtils.switchMapTransformer(map).transform("HELLO"));
        assertEquals("B", TransformerUtils.switchMapTransformer(map).transform("THERE"));
        map.put(null, c);
        assertEquals("C", TransformerUtils.switchMapTransformer(map).transform("WELL"));

        assertSame(ConstantTransformer.NULL_INSTANCE, TransformerUtils.switchMapTransformer(new HashMap<Object, Transformer<Object, Object>>()));
        map = new HashMap<String, Transformer<String, String>>();
        map.put(null, null);
        assertSame(ConstantTransformer.NULL_INSTANCE, TransformerUtils.switchMapTransformer(map));

        try {
            TransformerUtils.switchMapTransformer(null);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestTransformerUtils::testInvokerTransformer
    public void testInvokerTransformer() {
        List<Object> list = new ArrayList<Object>();
        assertEquals(new Integer(0), TransformerUtils.invokerTransformer("size").transform(list));
        list.add(new Object());
        assertEquals(new Integer(1), TransformerUtils.invokerTransformer("size").transform(list));
        assertEquals(null, TransformerUtils.invokerTransformer("size").transform(null));

        try {
            TransformerUtils.invokerTransformer(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.invokerTransformer("noSuchMethod").transform(new Object());
            fail();
        } catch (FunctorException ex) {}
    }

// org.apache.commons.collections.TestTransformerUtils::testInvokerTransformer2
    public void testInvokerTransformer2() {
        List<Object> list = new ArrayList<Object>();
        assertEquals(Boolean.FALSE, TransformerUtils.invokerTransformer("contains",
                new Class[] { Object.class }, new Object[] { cString }).transform(list));
        list.add(cString);
        assertEquals(Boolean.TRUE, TransformerUtils.invokerTransformer("contains",
                new Class[] { Object.class }, new Object[] { cString }).transform(list));
        assertEquals(null, TransformerUtils.invokerTransformer("contains",
                new Class[] { Object.class }, new Object[] { cString }).transform(null));

        try {
            TransformerUtils.invokerTransformer(null, null, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.invokerTransformer("noSuchMethod", new Class[] { Object.class },
                    new Object[] { cString }).transform(new Object());
            fail();
        } catch (FunctorException ex) {}
        try {
            TransformerUtils.invokerTransformer("badArgs", null, new Object[] { cString });
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.invokerTransformer("badArgs", new Class[] { Object.class }, null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.invokerTransformer("badArgs", new Class[] {}, new Object[] { cString });
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestTransformerUtils::testStringValueTransformer
    public void testStringValueTransformer() {
        assertNotNull( "StringValueTransformer should NEVER return a null value.",
           TransformerUtils.stringValueTransformer().transform(null));
        assertEquals( "StringValueTransformer should return \"null\" when given a null argument.", "null",
            TransformerUtils.stringValueTransformer().transform(null));
        assertEquals( "StringValueTransformer should return toString value", "6",
            TransformerUtils.stringValueTransformer().transform(new Integer(6)));
    }

// org.apache.commons.collections.TestTransformerUtils::testInstantiateTransformerNull
    public void testInstantiateTransformerNull() {
        try {
            TransformerUtils.instantiateTransformer(null, new Object[] { "str" });
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            TransformerUtils.instantiateTransformer(new Class[] {}, new Object[] { "str" });
            fail();
        } catch (IllegalArgumentException ex) {}

        Transformer<Class<?>, Object> trans = TransformerUtils.instantiateTransformer(new Class[] { Long.class }, new Object[] { null });
        try {
            trans.transform(String.class);
            fail();
        } catch (FunctorException ex) {}

        trans = TransformerUtils.instantiateTransformer();
        assertEquals("", trans.transform(String.class));

        trans = TransformerUtils.instantiateTransformer(new Class[] { Long.TYPE }, new Object[] { new Long(1000L) });
        assertEquals(new Date(1000L), trans.transform(Date.class));
    }

// org.apache.commons.collections.TestTransformerUtils::testSingletonPatternInSerialization
    public void testSingletonPatternInSerialization() {
        final Object[] singletones = new Object[] {
                CloneTransformer.INSTANCE,
                ExceptionTransformer.INSTANCE,
                NOPTransformer.INSTANCE,
                StringValueTransformer.INSTANCE,
        };

        for (final Object original : singletones) {
            TestUtils.assertSameAfterSerialization(
                    "Singletone patern broken for " + original.getClass(),
                    original
            );
        }
    }

// org.apache.commons.collections.functors.TestEqualPredicate::testNullArgumentEqualsNullPredicate
    public void testNullArgumentEqualsNullPredicate() throws Exception {
        assertSame(nullPredicate(), equalPredicate(null));
    }

// org.apache.commons.collections.functors.TestEqualPredicate::objectFactoryUsesEqualsForTest
    public void objectFactoryUsesEqualsForTest() throws Exception {
        Predicate<EqualsTestObject> predicate = equalPredicate(FALSE_OBJECT);
        assertFalse(predicate, FALSE_OBJECT);
        assertTrue(equalPredicate(TRUE_OBJECT), TRUE_OBJECT);
    }

// org.apache.commons.collections.functors.TestEqualPredicate::testPredicateTypeCanBeSuperClassOfObject
    public void testPredicateTypeCanBeSuperClassOfObject() throws Exception {
        Predicate<Number> predicate = equalPredicate((Number) 4);
        assertTrue(predicate, 4);
    }
