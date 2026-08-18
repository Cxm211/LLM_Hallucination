// buggy code
    public static Collection removeAll(Collection collection, Collection remove) {
        return ListUtils.retainAll(collection, remove);
    }

// relevant test
// org.apache.commons.collections.TestCollectionUtils::testGetCardinalityMap
    public void testGetCardinalityMap() {
        Map freq = CollectionUtils.getCardinalityMap(collectionA);
        assertEquals(new Integer(1),freq.get("a"));
        assertEquals(new Integer(2),freq.get("b"));
        assertEquals(new Integer(3),freq.get("c"));
        assertEquals(new Integer(4),freq.get("d"));
        assertNull(freq.get("e"));

        freq = CollectionUtils.getCardinalityMap(collectionB);
        assertNull(freq.get("a"));
        assertEquals(new Integer(4),freq.get("b"));
        assertEquals(new Integer(3),freq.get("c"));
        assertEquals(new Integer(2),freq.get("d"));
        assertEquals(new Integer(1),freq.get("e"));
    }

// org.apache.commons.collections.TestCollectionUtils::testCardinality
    public void testCardinality() {
        assertEquals(1, CollectionUtils.cardinality("a", collectionA));
        assertEquals(2, CollectionUtils.cardinality("b", collectionA));
        assertEquals(3, CollectionUtils.cardinality("c", collectionA));
        assertEquals(4, CollectionUtils.cardinality("d", collectionA));
        assertEquals(0, CollectionUtils.cardinality("e", collectionA));

        assertEquals(0, CollectionUtils.cardinality("a", collectionB));
        assertEquals(4, CollectionUtils.cardinality("b", collectionB));
        assertEquals(3, CollectionUtils.cardinality("c", collectionB));
        assertEquals(2, CollectionUtils.cardinality("d", collectionB));
        assertEquals(1, CollectionUtils.cardinality("e", collectionB));

        Set set = new HashSet();
        set.add("A");
        set.add("C");
        set.add("E");
        set.add("E");
        assertEquals(1, CollectionUtils.cardinality("A", set));
        assertEquals(0, CollectionUtils.cardinality("B", set));
        assertEquals(1, CollectionUtils.cardinality("C", set));
        assertEquals(0, CollectionUtils.cardinality("D", set));
        assertEquals(1, CollectionUtils.cardinality("E", set));

        Bag bag = new HashBag();
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

// org.apache.commons.collections.TestCollectionUtils::testCardinalityOfNull
    public void testCardinalityOfNull() {
        List list = new ArrayList();
        assertEquals(0,CollectionUtils.cardinality(null,list));
        {
            Map freq = CollectionUtils.getCardinalityMap(list);
            assertNull(freq.get(null));
        }
        list.add("A");
        assertEquals(0,CollectionUtils.cardinality(null,list));
        {
            Map freq = CollectionUtils.getCardinalityMap(list);
            assertNull(freq.get(null));
        }
        list.add(null);
        assertEquals(1,CollectionUtils.cardinality(null,list));
        {
            Map freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(new Integer(1),freq.get(null));
        }
        list.add("B");
        assertEquals(1,CollectionUtils.cardinality(null,list));
        {
            Map freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(new Integer(1),freq.get(null));
        }
        list.add(null);
        assertEquals(2,CollectionUtils.cardinality(null,list));
        {
            Map freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(new Integer(2),freq.get(null));
        }
        list.add("B");
        assertEquals(2,CollectionUtils.cardinality(null,list));
        {
            Map freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(new Integer(2),freq.get(null));
        }
        list.add(null);
        assertEquals(3,CollectionUtils.cardinality(null,list));
        {
            Map freq = CollectionUtils.getCardinalityMap(list);
            assertEquals(new Integer(3),freq.get(null));
        }
    }

// org.apache.commons.collections.TestCollectionUtils::testContainsAny
    public void testContainsAny() {
        Collection empty = new ArrayList(0);
        Collection one = new ArrayList(1);
        one.add("1");
        Collection two = new ArrayList(1);
        two.add("2");
        Collection three = new ArrayList(1);
        three.add("3");
        Collection odds = new ArrayList(2);
        odds.add("1");
        odds.add("3");
        
        assertTrue("containsAny({1},{1,3}) should return true.",
            CollectionUtils.containsAny(one,odds));
        assertTrue("containsAny({1,3},{1}) should return true.",
            CollectionUtils.containsAny(odds,one));
        assertTrue("containsAny({3},{1,3}) should return true.",
            CollectionUtils.containsAny(three,odds));
        assertTrue("containsAny({1,3},{3}) should return true.",
            CollectionUtils.containsAny(odds,three));
        assertTrue("containsAny({2},{2}) should return true.",
            CollectionUtils.containsAny(two,two));
        assertTrue("containsAny({1,3},{1,3}) should return true.",
            CollectionUtils.containsAny(odds,odds));
        
        assertTrue("containsAny({2},{1,3}) should return false.",
            !CollectionUtils.containsAny(two,odds));
        assertTrue("containsAny({1,3},{2}) should return false.",
            !CollectionUtils.containsAny(odds,two));
        assertTrue("containsAny({1},{3}) should return false.",
            !CollectionUtils.containsAny(one,three));
        assertTrue("containsAny({3},{1}) should return false.",
            !CollectionUtils.containsAny(three,one));
        assertTrue("containsAny({1,3},{}) should return false.",
            !CollectionUtils.containsAny(odds,empty));
        assertTrue("containsAny({},{1,3}) should return false.",
            !CollectionUtils.containsAny(empty,odds));
        assertTrue("containsAny({},{}) should return false.",
            !CollectionUtils.containsAny(empty,empty));
    }

// org.apache.commons.collections.TestCollectionUtils::testUnion
    public void testUnion() {
        Collection col = CollectionUtils.union(collectionA,collectionB);
        Map freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(new Integer(1),freq.get("a"));
        assertEquals(new Integer(4),freq.get("b"));
        assertEquals(new Integer(3),freq.get("c"));
        assertEquals(new Integer(4),freq.get("d"));
        assertEquals(new Integer(1),freq.get("e"));

        Collection col2 = CollectionUtils.union(collectionB,collectionA);
        Map freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(new Integer(1),freq2.get("a"));
        assertEquals(new Integer(4),freq2.get("b"));
        assertEquals(new Integer(3),freq2.get("c"));
        assertEquals(new Integer(4),freq2.get("d"));
        assertEquals(new Integer(1),freq2.get("e"));        
    }

// org.apache.commons.collections.TestCollectionUtils::testIntersection
    public void testIntersection() {
        Collection col = CollectionUtils.intersection(collectionA,collectionB);
        Map freq = CollectionUtils.getCardinalityMap(col);
        assertNull(freq.get("a"));
        assertEquals(new Integer(2),freq.get("b"));
        assertEquals(new Integer(3),freq.get("c"));
        assertEquals(new Integer(2),freq.get("d"));
        assertNull(freq.get("e"));

        Collection col2 = CollectionUtils.intersection(collectionB,collectionA);
        Map freq2 = CollectionUtils.getCardinalityMap(col2);
        assertNull(freq2.get("a"));
        assertEquals(new Integer(2),freq2.get("b"));
        assertEquals(new Integer(3),freq2.get("c"));
        assertEquals(new Integer(2),freq2.get("d"));
        assertNull(freq2.get("e"));      
    }

// org.apache.commons.collections.TestCollectionUtils::testDisjunction
    public void testDisjunction() {
        Collection col = CollectionUtils.disjunction(collectionA,collectionB);
        Map freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(new Integer(1),freq.get("a"));
        assertEquals(new Integer(2),freq.get("b"));
        assertNull(freq.get("c"));
        assertEquals(new Integer(2),freq.get("d"));
        assertEquals(new Integer(1),freq.get("e"));

        Collection col2 = CollectionUtils.disjunction(collectionB,collectionA);
        Map freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(new Integer(1),freq2.get("a"));
        assertEquals(new Integer(2),freq2.get("b"));
        assertNull(freq2.get("c"));
        assertEquals(new Integer(2),freq2.get("d"));
        assertEquals(new Integer(1),freq2.get("e"));
    }

// org.apache.commons.collections.TestCollectionUtils::testDisjunctionAsUnionMinusIntersection
    public void testDisjunctionAsUnionMinusIntersection() {
        Collection dis = CollectionUtils.disjunction(collectionA,collectionB);
        Collection un = CollectionUtils.union(collectionA,collectionB);
        Collection inter = CollectionUtils.intersection(collectionA,collectionB);
        assertTrue(CollectionUtils.isEqualCollection(dis,CollectionUtils.subtract(un,inter)));
    }

// org.apache.commons.collections.TestCollectionUtils::testDisjunctionAsSymmetricDifference
    public void testDisjunctionAsSymmetricDifference() {
        Collection dis = CollectionUtils.disjunction(collectionA,collectionB);
        Collection amb = CollectionUtils.subtract(collectionA,collectionB);
        Collection bma = CollectionUtils.subtract(collectionB,collectionA);
        assertTrue(CollectionUtils.isEqualCollection(dis,CollectionUtils.union(amb,bma)));
    }

// org.apache.commons.collections.TestCollectionUtils::testSubtract
    public void testSubtract() {
        Collection col = CollectionUtils.subtract(collectionA,collectionB);
        Map freq = CollectionUtils.getCardinalityMap(col);
        assertEquals(new Integer(1),freq.get("a"));
        assertNull(freq.get("b"));
        assertNull(freq.get("c"));
        assertEquals(new Integer(2),freq.get("d"));
        assertNull(freq.get("e"));

        Collection col2 = CollectionUtils.subtract(collectionB,collectionA);
        Map freq2 = CollectionUtils.getCardinalityMap(col2);
        assertEquals(new Integer(1),freq2.get("e"));
        assertNull(freq2.get("d"));
        assertNull(freq2.get("c"));
        assertEquals(new Integer(2),freq2.get("b"));
        assertNull(freq2.get("a"));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsSubCollectionOfSelf
    public void testIsSubCollectionOfSelf() {
        assertTrue(CollectionUtils.isSubCollection(collectionA,collectionA));
        assertTrue(CollectionUtils.isSubCollection(collectionB,collectionB));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsSubCollection
    public void testIsSubCollection() {
        assertTrue(!CollectionUtils.isSubCollection(collectionA,collectionB));
        assertTrue(!CollectionUtils.isSubCollection(collectionB,collectionA));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsSubCollection2
    public void testIsSubCollection2() {
        Collection c = new ArrayList();
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("a");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("b");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("b");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("c");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("c");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("c");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("d");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("d");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("d");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(!CollectionUtils.isSubCollection(collectionA,c));
        c.add("d");
        assertTrue(CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(CollectionUtils.isSubCollection(collectionA,c));
        c.add("e");
        assertTrue(!CollectionUtils.isSubCollection(c,collectionA));
        assertTrue(CollectionUtils.isSubCollection(collectionA,c));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEqualCollectionToSelf
    public void testIsEqualCollectionToSelf() {
        assertTrue(CollectionUtils.isEqualCollection(collectionA,collectionA));
        assertTrue(CollectionUtils.isEqualCollection(collectionB,collectionB));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEqualCollection
    public void testIsEqualCollection() {
        assertTrue(!CollectionUtils.isEqualCollection(collectionA,collectionB));
        assertTrue(!CollectionUtils.isEqualCollection(collectionB,collectionA));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEqualCollection2
    public void testIsEqualCollection2() {
        Collection a = new ArrayList();
        Collection b = new ArrayList();
        assertTrue(CollectionUtils.isEqualCollection(a,b));
        assertTrue(CollectionUtils.isEqualCollection(b,a));
        a.add("1");
        assertTrue(!CollectionUtils.isEqualCollection(a,b));
        assertTrue(!CollectionUtils.isEqualCollection(b,a));
        b.add("1");
        assertTrue(CollectionUtils.isEqualCollection(a,b));
        assertTrue(CollectionUtils.isEqualCollection(b,a));
        a.add("2");
        assertTrue(!CollectionUtils.isEqualCollection(a,b));
        assertTrue(!CollectionUtils.isEqualCollection(b,a));
        b.add("2");
        assertTrue(CollectionUtils.isEqualCollection(a,b));
        assertTrue(CollectionUtils.isEqualCollection(b,a));
        a.add("1");
        assertTrue(!CollectionUtils.isEqualCollection(a,b));
        assertTrue(!CollectionUtils.isEqualCollection(b,a));
        b.add("1");
        assertTrue(CollectionUtils.isEqualCollection(a,b));
        assertTrue(CollectionUtils.isEqualCollection(b,a));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsProperSubCollection
    public void testIsProperSubCollection() {
        Collection a = new ArrayList();
        Collection b = new ArrayList();
        assertTrue(!CollectionUtils.isProperSubCollection(a,b));
        b.add("1");
        assertTrue(CollectionUtils.isProperSubCollection(a,b));
        assertTrue(!CollectionUtils.isProperSubCollection(b,a));
        assertTrue(!CollectionUtils.isProperSubCollection(b,b));
        assertTrue(!CollectionUtils.isProperSubCollection(a,a));
        a.add("1");
        a.add("2");
        b.add("2");
        assertTrue(!CollectionUtils.isProperSubCollection(b,a));
        assertTrue(!CollectionUtils.isProperSubCollection(a,b));
        a.add("1");
        assertTrue(CollectionUtils.isProperSubCollection(b,a));
        assertTrue(CollectionUtils.isProperSubCollection(
            CollectionUtils.intersection(collectionA, collectionB), collectionA));
        assertTrue(CollectionUtils.isProperSubCollection(
            CollectionUtils.subtract(a, b), a));
        assertTrue(!CollectionUtils.isProperSubCollection(
            a, CollectionUtils.subtract(a, b)));
    }

// org.apache.commons.collections.TestCollectionUtils::testFind
    public void testFind() {
        Predicate testPredicate = PredicateUtils.equalPredicate("d");
        Object test = CollectionUtils.find(collectionA, testPredicate);
        assertTrue(test.equals("d"));
        testPredicate = PredicateUtils.equalPredicate("de");
        test = CollectionUtils.find(collectionA, testPredicate);
        assertTrue(test == null);
        assertEquals(CollectionUtils.find(null,testPredicate), null);
        assertEquals(CollectionUtils.find(collectionA, null), null);
    }

// org.apache.commons.collections.TestCollectionUtils::testForAllDo
    public void testForAllDo() {
        Closure testClosure = ClosureUtils.invokerClosure("clear");
        Collection col = new ArrayList();
        col.add(collectionA);
        col.add(collectionB);
        CollectionUtils.forAllDo(col, testClosure);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        CollectionUtils.forAllDo(col, null);
        assertTrue(collectionA.isEmpty() && collectionB.isEmpty());
        CollectionUtils.forAllDo(null, testClosure);
        col.add(null);
        
        CollectionUtils.forAllDo(col, testClosure);
        col.add("x");
        
        try {
            CollectionUtils.forAllDo(col, testClosure);
            fail("Expecting FunctorException");
        } catch (FunctorException ex) {
            
        }
    }

// org.apache.commons.collections.TestCollectionUtils::testIndex
    public void testIndex() {     
        
        Map map = new HashMap();
        map.put(new Integer(0), "zero");
        map.put(new Integer(-1), "minusOne");
        Object test = CollectionUtils.index(map, 0);
        assertTrue(test.equals("zero"));
        test = CollectionUtils.index(map, new Integer(-1));
        assertTrue(test.equals("minusOne"));
        
        
        test = CollectionUtils.index(map, "missing");
        assertTrue(test.equals(map));
        
        
        test = CollectionUtils.index(map, new Integer(1));   
        assertTrue(map.keySet().contains(test)); 
        
        
        test = CollectionUtils.index(map, new Integer(4));         
        assertTrue((test instanceof Iterator) && !((Iterator) test).hasNext());  

        
        SortedMap map2 = new TreeMap();
        map2.put(new Integer(23), "u");
        map2.put(new Integer(21), "x");
        map2.put(new Integer(17), "v");
        map2.put(new Integer(42), "w");
        Integer val = (Integer) CollectionUtils.index(map2, 0);
        assertTrue(val.intValue() == 17);
        val = (Integer) CollectionUtils.index(map2, 1);
        assertTrue(val.intValue() == 21);
        val = (Integer) CollectionUtils.index(map2, 2);
        assertTrue(val.intValue() == 23);
        val = (Integer) CollectionUtils.index(map2, 3);
        assertTrue(val.intValue() == 42);   
                
        
        List list = new ArrayList();
        list.add("zero");
        list.add("one");
        test = CollectionUtils.index(list, 0);
        assertTrue(test.equals("zero"));
        test = CollectionUtils.index(list, 1);
        assertTrue(test.equals("one"));
        
        
        try {
            test = CollectionUtils.index(list, 2);
            fail("Expecting IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            
        }
        
        
        Iterator iterator = list.iterator();
        test = CollectionUtils.index(iterator,0);
        assertTrue(test.equals("zero"));
        iterator = list.iterator();
        test = CollectionUtils.index(iterator,1);
        assertTrue(test.equals("one"));
        
        
        test = CollectionUtils.index(iterator,3);
        assertTrue(test.equals(iterator) && !iterator.hasNext());
        
        
        Vector vector = new Vector(list);
        Enumeration en = vector.elements();
        test = CollectionUtils.index(en,0);
        assertTrue(test.equals("zero"));
        en = vector.elements();
        test = CollectionUtils.index(en,1);
        assertTrue(test.equals("one"));
        
        
        test = CollectionUtils.index(en,3);
        assertTrue(test.equals(en) && !en.hasMoreElements());
        
        
        Bag bag = new HashBag();
        bag.add("element", 1);
        test = CollectionUtils.index(bag, 0);
        assertTrue(test.equals("element"));
        
        
        test = CollectionUtils.index(bag, 2);
        assertTrue((test instanceof Iterator) && !((Iterator) test).hasNext()); 
        
        
        Object[] objArray = new Object[2];
        objArray[0] = "zero";
        objArray[1] = "one";
        test = CollectionUtils.index(objArray,0);
        assertTrue(test.equals("zero"));
        test = CollectionUtils.index(objArray,1);
        assertTrue(test.equals("one"));
        
        
        try {
            test = CollectionUtils.index(objArray,2);
            fail("Expecting ArrayIndexOutOfBoundsException.");
        } catch (ArrayIndexOutOfBoundsException ex) {
            
        }
        
        
        Object obj = new Object();
        test = CollectionUtils.index(obj, obj);
        assertTrue(test.equals(obj));
    }

// org.apache.commons.collections.TestCollectionUtils::testGet
    public void testGet() {     
        {
            
            Map expected = new HashMap();
            expected.put("zeroKey", "zero");
            expected.put("oneKey", "one");
        
            Map found = new HashMap();
            Map.Entry entry = (Map.Entry)(CollectionUtils.get(expected, 0));
            found.put(entry.getKey(),entry.getValue());
            entry = (Map.Entry)(CollectionUtils.get(expected, 1));
            found.put(entry.getKey(),entry.getValue());
            assertEquals(expected,found);
        
            
            try {
                CollectionUtils.get(expected,  2);
                fail("Expecting IndexOutOfBoundsException.");
            } catch (IndexOutOfBoundsException e) {
                
            }
            try {
                CollectionUtils.get(expected,  -2);
                fail("Expecting IndexOutOfBoundsException.");
            } catch (IndexOutOfBoundsException e) {
                
            }
        }

        {
            
            SortedMap map = new TreeMap();
            map.put("zeroKey", "zero");
            map.put("oneKey", "one");
            Object test = CollectionUtils.get(map, 1);
            assertEquals("zeroKey",((Map.Entry) test).getKey());
            assertEquals("zero",((Map.Entry) test).getValue());
            test = CollectionUtils.get(map, 0);
            assertEquals("oneKey",((Map.Entry) test).getKey());
            assertEquals("one",((Map.Entry) test).getValue());
        }
                
        {
            
            List list = new ArrayList();
            list.add("zero");
            list.add("one");
            assertEquals("zero",CollectionUtils.get(list, 0));
            assertEquals("one",CollectionUtils.get(list, 1));
            
            try {
                CollectionUtils.get(list, 2);
                fail("Expecting IndexOutOfBoundsException");
            } catch (IndexOutOfBoundsException e) {
                
            }

            
            Iterator iterator = list.iterator();
            assertEquals("zero",CollectionUtils.get(iterator,0));
            iterator = list.iterator();
            assertEquals("one",CollectionUtils.get(iterator,1));
        
            
            try {
                CollectionUtils.get(iterator,3);
                fail("Expecting IndexOutOfBoundsException.");
            } catch (IndexOutOfBoundsException e) {
                
            }
            assertTrue(!iterator.hasNext());
        }
        
        {
            
            Vector vector = new Vector();
            vector.addElement("zero");
            vector.addElement("one");
            Enumeration en = vector.elements();
            assertEquals("zero",CollectionUtils.get(en,0));
            en = vector.elements();
            assertEquals("one",CollectionUtils.get(en,1));
        
            
            try {
                CollectionUtils.get(en,3);
                fail("Expecting IndexOutOfBoundsException.");
            } catch (IndexOutOfBoundsException e) {
                
            }
            assertTrue(!en.hasMoreElements());
        }
        
        {
            
            Bag bag = new HashBag();
            bag.add("element", 1);
            assertEquals("element",CollectionUtils.get(bag, 0));
        
            
            try {
                CollectionUtils.get(bag, 1);
                fail("Expceting IndexOutOfBoundsException.");
            } catch (IndexOutOfBoundsException e) {
                
            }
        }
        
        {
            
            Object[] objArray = new Object[2];
            objArray[0] = "zero";
            objArray[1] = "one";
            assertEquals("zero",CollectionUtils.get(objArray,0));
            assertEquals("one",CollectionUtils.get(objArray,1));
        
            
            try {
                CollectionUtils.get(objArray,2);
                fail("Expecting IndexOutOfBoundsException.");
            } catch (IndexOutOfBoundsException ex) {
                
            }
        }
        
        {
            
            int[] array = new int[2];
            array[0] = 10;
            array[1] = 20;
            assertEquals(new Integer(10), CollectionUtils.get(array,0));
            assertEquals(new Integer(20), CollectionUtils.get(array,1));
        
            
            try {
                CollectionUtils.get(array,2);
                fail("Expecting IndexOutOfBoundsException.");
            } catch (IndexOutOfBoundsException ex) {
                
            }
        }
        
        {
            
            Object obj = new Object();
            try {
                CollectionUtils.get(obj, 0);
                fail("Expecting IllegalArgumentException.");
            } catch (IllegalArgumentException e) {
                
            }
            try {
                CollectionUtils.get(null, 0);
                fail("Expecting IllegalArgumentException.");
            } catch (IllegalArgumentException e) {
                
            }
        }
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_List
    public void testSize_List() {
        List list = new ArrayList();
        assertEquals(0, CollectionUtils.size(list));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_Map
    public void testSize_Map() {
        Map map = new HashMap();
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
        Vector list = new Vector();
        assertEquals(0, CollectionUtils.size(list.elements()));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list.elements()));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list.elements()));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_Iterator
    public void testSize_Iterator() {
        List list = new ArrayList();
        assertEquals(0, CollectionUtils.size(list.iterator()));
        list.add("a");
        assertEquals(1, CollectionUtils.size(list.iterator()));
        list.add("b");
        assertEquals(2, CollectionUtils.size(list.iterator()));
    }

// org.apache.commons.collections.TestCollectionUtils::testSize_Other
    public void testSize_Other() {
        try {
            CollectionUtils.size(null);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
        try {
            CollectionUtils.size("not a list");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException e) {}
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_List
    public void testSizeIsEmpty_List() {
        List list = new ArrayList();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Map
    public void testSizeIsEmpty_Map() {
        Map map = new HashMap();
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
        Vector list = new Vector();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list.elements()));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list.elements()));
        Enumeration en = list.elements();
        en.nextElement();
        assertEquals(true, CollectionUtils.sizeIsEmpty(en));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Iterator
    public void testSizeIsEmpty_Iterator() {
        List list = new ArrayList();
        assertEquals(true, CollectionUtils.sizeIsEmpty(list.iterator()));
        list.add("a");
        assertEquals(false, CollectionUtils.sizeIsEmpty(list.iterator()));
        Iterator it = list.iterator();
        it.next();
        assertEquals(true, CollectionUtils.sizeIsEmpty(it));
    }

// org.apache.commons.collections.TestCollectionUtils::testSizeIsEmpty_Other
    public void testSizeIsEmpty_Other() {
        try {
            CollectionUtils.sizeIsEmpty(null);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}
        try {
            CollectionUtils.sizeIsEmpty("not a list");
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEmptyWithEmptyCollection
    public void testIsEmptyWithEmptyCollection() {
        Collection coll = new ArrayList();
        assertEquals(true, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEmptyWithNonEmptyCollection
    public void testIsEmptyWithNonEmptyCollection() {
        Collection coll = new ArrayList();
        coll.add("item");
        assertEquals(false, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsEmptyWithNull
    public void testIsEmptyWithNull() {
        Collection coll = null;
        assertEquals(true, CollectionUtils.isEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsNotEmptyWithEmptyCollection
    public void testIsNotEmptyWithEmptyCollection() {
        Collection coll = new ArrayList();
        assertEquals(false, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsNotEmptyWithNonEmptyCollection
    public void testIsNotEmptyWithNonEmptyCollection() {
        Collection coll = new ArrayList();
        coll.add("item");
        assertEquals(true, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testIsNotEmptyWithNull
    public void testIsNotEmptyWithNull() {
        Collection coll = null;
        assertEquals(false, CollectionUtils.isNotEmpty(coll));
    }

// org.apache.commons.collections.TestCollectionUtils::testFilter
    public void testFilter() {
        List list = new ArrayList();
        list.add("One");
        list.add("Two");
        list.add("Three");
        list.add("Four");
        CollectionUtils.filter(list, EQUALS_TWO);
        assertEquals(1, list.size());
        assertEquals("Two", list.get(0));
        
        list = new ArrayList();
        list.add("One");
        list.add("Two");
        list.add("Three");
        list.add("Four");
        CollectionUtils.filter(list, null);
        assertEquals(4, list.size());
        CollectionUtils.filter(null, EQUALS_TWO);
        assertEquals(4, list.size());
        CollectionUtils.filter(null, null);
        assertEquals(4, list.size());
    }

// org.apache.commons.collections.TestCollectionUtils::testCountMatches
    public void testCountMatches() {
        List list = new ArrayList();
        list.add("One");
        list.add("Two");
        list.add("Three");
        list.add("Four");
        int count = CollectionUtils.countMatches(list, EQUALS_TWO);
        assertEquals(4, list.size());
        assertEquals(1, count);
        assertEquals(0, CollectionUtils.countMatches(list, null));
        assertEquals(0, CollectionUtils.countMatches(null, EQUALS_TWO));
        assertEquals(0, CollectionUtils.countMatches(null, null));
    }

// org.apache.commons.collections.TestCollectionUtils::testExists
    public void testExists() {
        List list = new ArrayList();
        assertEquals(false, CollectionUtils.exists(null, null));
        assertEquals(false, CollectionUtils.exists(list, null));
        assertEquals(false, CollectionUtils.exists(null, EQUALS_TWO));
        assertEquals(false, CollectionUtils.exists(list, EQUALS_TWO));
        list.add("One");
        list.add("Three");
        list.add("Four");
        assertEquals(false, CollectionUtils.exists(list, EQUALS_TWO));

        list.add("Two");
        assertEquals(true, CollectionUtils.exists(list, EQUALS_TWO));
    }

// org.apache.commons.collections.TestCollectionUtils::testSelect
    public void testSelect() {
        List list = new ArrayList();
        list.add("One");
        list.add("Two");
        list.add("Three");
        list.add("Four");
        Collection output = CollectionUtils.select(list, EQUALS_TWO);
        assertEquals(4, list.size());
        assertEquals(1, output.size());
        assertEquals("Two", output.iterator().next());
    }

// org.apache.commons.collections.TestCollectionUtils::testSelectRejected
    public void testSelectRejected() {
        List list = new ArrayList();
        list.add("One");
        list.add("Two");
        list.add("Three");
        list.add("Four");
        Collection output = CollectionUtils.selectRejected(list, EQUALS_TWO);
        assertEquals(4, list.size());
        assertEquals(3, output.size());
        assertTrue(output.contains("One"));
        assertTrue(output.contains("Three"));
        assertTrue(output.contains("Four"));
    }

// org.apache.commons.collections.TestCollectionUtils::testCollect
    public void testCollect() {
        Transformer transformer = TransformerUtils.constantTransformer("z");
        Collection collection = CollectionUtils.collect(collectionA, transformer);
        assertTrue(collection.size() == collectionA.size());
        assertTrue(collectionA.contains("a") && ! collectionA.contains("z"));
        assertTrue(collection.contains("z") && !collection.contains("a"));
        
        collection = new ArrayList();
        CollectionUtils.collect(collectionA, transformer, collection);
        assertTrue(collection.size() == collectionA.size());
        assertTrue(collectionA.contains("a") && ! collectionA.contains("z"));
        assertTrue(collection.contains("z") && !collection.contains("a"));
        
        Iterator iterator = null;
        collection = new ArrayList();
        CollectionUtils.collect(iterator, transformer, collection);
        
        iterator = collectionA.iterator();
        CollectionUtils.collect(iterator, transformer, collection);
        assertTrue(collection.size() == collectionA.size());
        assertTrue(collectionA.contains("a") && ! collectionA.contains("z"));
        assertTrue(collection.contains("z") && !collection.contains("a")); 
        
        iterator = collectionA.iterator();
        collection = CollectionUtils.collect(iterator, transformer);
        assertTrue(collection.size() == collectionA.size());
        assertTrue(collection.contains("z") && !collection.contains("a")); 
        collection = CollectionUtils.collect((Iterator) null, (Transformer) null);
        assertTrue(collection.size() == 0);
           
        int size = collectionA.size();
        CollectionUtils.collect((Collection) null, transformer, collectionA);
        assertTrue(collectionA.size() == size && collectionA.contains("a"));
        CollectionUtils.collect(collectionB, null, collectionA);
        assertTrue(collectionA.size() == size && collectionA.contains("a"));
        
    }

// org.apache.commons.collections.TestCollectionUtils::testTransform1
    public void testTransform1() {
        List list = new ArrayList();
        list.add("1");
        list.add("2");
        list.add("3");
        CollectionUtils.transform(list, TRANSFORM_TO_INTEGER);
        assertEquals(3, list.size());
        assertEquals(new Integer(1), list.get(0));
        assertEquals(new Integer(2), list.get(1));
        assertEquals(new Integer(3), list.get(2));
        
        list = new ArrayList();
        list.add("1");
        list.add("2");
        list.add("3");
        CollectionUtils.transform(null, TRANSFORM_TO_INTEGER);
        assertEquals(3, list.size());
        CollectionUtils.transform(list, null);
        assertEquals(3, list.size());
        CollectionUtils.transform(null, null);
        assertEquals(3, list.size());
    }

// org.apache.commons.collections.TestCollectionUtils::testTransform2
    public void testTransform2() {
        Set set = new HashSet();
        set.add("1");
        set.add("2");
        set.add("3");
        CollectionUtils.transform(set, new Transformer() {
            public Object transform(Object input) {
                return new Integer(4);
            }
        });
        assertEquals(1, set.size());
        assertEquals(new Integer(4), set.iterator().next());
    }

// org.apache.commons.collections.TestCollectionUtils::testAddIgnoreNull
    public void testAddIgnoreNull() {
        Set set = new HashSet();
        set.add("1");
        set.add("2");
        set.add("3");
        assertEquals(false, CollectionUtils.addIgnoreNull(set, null));
        assertEquals(3, set.size());
        assertEquals(false, CollectionUtils.addIgnoreNull(set, "1"));
        assertEquals(3, set.size());
        assertEquals(true, CollectionUtils.addIgnoreNull(set, "4"));
        assertEquals(4, set.size());
        assertEquals(true, set.contains("4"));
    }

// org.apache.commons.collections.TestCollectionUtils::testPredicatedCollection
    public void testPredicatedCollection() {
        Predicate predicate = new Predicate() {
            public boolean evaluate(Object o) {
                return o instanceof String;
            }
        };
        Collection collection = 
            CollectionUtils.predicatedCollection(new ArrayList(), predicate);
        assertTrue("returned object should be a PredicatedCollection",
            collection instanceof PredicatedCollection);
        try { 
           collection = 
                CollectionUtils.predicatedCollection(new ArrayList(), null); 
           fail("Expecting IllegalArgumentException for null predicate.");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
           collection = 
                CollectionUtils.predicatedCollection(null, predicate); 
           fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }             
    }

// org.apache.commons.collections.TestCollectionUtils::testIsFull
    public void testIsFull() {
        Set set = new HashSet();
        set.add("1");
        set.add("2");
        set.add("3");
        try {
            CollectionUtils.isFull(null);
            fail();
        } catch (NullPointerException ex) {}
        assertEquals(false, CollectionUtils.isFull(set));
        
        BoundedFifoBuffer buf = new BoundedFifoBuffer(set);
        assertEquals(true, CollectionUtils.isFull(buf));
        buf.remove("2");
        assertEquals(false, CollectionUtils.isFull(buf));
        buf.add("2");
        assertEquals(true, CollectionUtils.isFull(buf));
        
        Buffer buf2 = BufferUtils.synchronizedBuffer(buf);
        assertEquals(true, CollectionUtils.isFull(buf2));
        buf2.remove("2");
        assertEquals(false, CollectionUtils.isFull(buf2));
        buf2.add("2");
        assertEquals(true, CollectionUtils.isFull(buf2));
    }

// org.apache.commons.collections.TestCollectionUtils::testMaxSize
    public void testMaxSize() {
        Set set = new HashSet();
        set.add("1");
        set.add("2");
        set.add("3");
        try {
            CollectionUtils.maxSize(null);
            fail();
        } catch (NullPointerException ex) {}
        assertEquals(-1, CollectionUtils.maxSize(set));
        
        BoundedFifoBuffer buf = new BoundedFifoBuffer(set);
        assertEquals(3, CollectionUtils.maxSize(buf));
        buf.remove("2");
        assertEquals(3, CollectionUtils.maxSize(buf));
        buf.add("2");
        assertEquals(3, CollectionUtils.maxSize(buf));
        
        Buffer buf2 = BufferUtils.synchronizedBuffer(buf);
        assertEquals(3, CollectionUtils.maxSize(buf2));
        buf2.remove("2");
        assertEquals(3, CollectionUtils.maxSize(buf2));
        buf2.add("2");
        assertEquals(3, CollectionUtils.maxSize(buf2));
    }

// org.apache.commons.collections.TestCollectionUtils::testIntersectionUsesMethodEquals
    public void testIntersectionUsesMethodEquals() {
        
        Object elta = new Integer(17);
        Object eltb = new Integer(17);
        
        
        assertEquals(elta,eltb);
        assertEquals(eltb,elta);
        
        
        assertTrue(elta != eltb);
        
        
        Collection cola = new ArrayList();
        Collection colb = new ArrayList();
        
        
        
        cola.add(elta);
        colb.add(eltb);
        
        
        
        Collection intersection = CollectionUtils.intersection(cola,colb);
        assertEquals(1,intersection.size());
        
        
        
        
        Object eltc = intersection.iterator().next();
        assertTrue((eltc == elta  && eltc != eltb) || (eltc != elta  && eltc == eltb));
        
        
        
        assertEquals(elta,eltc);
        assertEquals(eltc,elta);
        assertEquals(eltb,eltc);
        assertEquals(eltc,eltb);
    }

// org.apache.commons.collections.TestCollectionUtils::testRetainAll
    public void testRetainAll() {
        List base = new ArrayList();
        base.add("A");
        base.add("B");
        base.add("C");
        List sub = new ArrayList();
        sub.add("A");
        sub.add("C");
        sub.add("X");
        
        Collection result = CollectionUtils.retainAll(base, sub);
        assertEquals(2, result.size());
        assertEquals(true, result.contains("A"));
        assertEquals(false, result.contains("B"));
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
        } catch(NullPointerException npe){} 
    }

// org.apache.commons.collections.TestCollectionUtils::testRemoveAll
    public void testRemoveAll() {
        List base = new ArrayList();
        base.add("A");
        base.add("B");
        base.add("C");
        List sub = new ArrayList();
        sub.add("A");
        sub.add("C");
        sub.add("X");
        
        Collection result = CollectionUtils.removeAll(base, sub);
        assertEquals(1, result.size());
        assertEquals(false, result.contains("A"));
        assertEquals(true, result.contains("B"));
        assertEquals(false, result.contains("C"));
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
        } catch(NullPointerException npe){} 
    }

// org.apache.commons.collections.TestCollectionUtils::testTransformedCollection
    public void testTransformedCollection() {
        Transformer transformer = TransformerUtils.nopTransformer();
        Collection collection = 
            CollectionUtils.transformedCollection(new ArrayList(), transformer);
        assertTrue("returned object should be a TransformedCollection",
            collection instanceof TransformedCollection);
        try { 
           collection = 
                CollectionUtils.transformedCollection(new ArrayList(), null); 
           fail("Expecting IllegalArgumentException for null transformer.");
        } catch (IllegalArgumentException ex) {
            
        }
        try { 
           collection = 
                CollectionUtils.transformedCollection(null, transformer); 
           fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }             
    }

// org.apache.commons.collections.TestCollectionUtils::testTransformedCollection_2
    public void testTransformedCollection_2() {
        List list = new ArrayList();
        list.add("1");
        list.add("2");
        list.add("3");
        Collection result = CollectionUtils.transformedCollection(list, TRANSFORM_TO_INTEGER);
        assertEquals(true, result.contains("1"));  
        assertEquals(true, result.contains("2"));  
        assertEquals(true, result.contains("3"));  
    }

// org.apache.commons.collections.TestCollectionUtils::testSynchronizedCollection
    public void testSynchronizedCollection() {
        Collection col = CollectionUtils.synchronizedCollection(new ArrayList());
        assertTrue("Returned object should be a SynchronizedCollection.",
            col instanceof SynchronizedCollection);
        try {
            col = CollectionUtils.synchronizedCollection(null);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }  
    }

// org.apache.commons.collections.TestCollectionUtils::testUnmodifiableCollection
    public void testUnmodifiableCollection() {
        Collection col = CollectionUtils.unmodifiableCollection(new ArrayList());
        assertTrue("Returned object should be a UnmodifiableCollection.",
            col instanceof UnmodifiableCollection);
        try {
            col = CollectionUtils.unmodifiableCollection(null);
            fail("Expecting IllegalArgumentException for null collection.");
        } catch (IllegalArgumentException ex) {
            
        }  
    }

// org.apache.commons.collections.map.TestCompositeMap::testGet
    public void testGet() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo());
        Assert.assertEquals("one", map.get("1"));
        Assert.assertEquals("four", map.get("4"));
    }

// org.apache.commons.collections.map.TestCompositeMap::testAddComposited
    public void testAddComposited() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo());
        HashMap three = new HashMap();
        three.put("5", "five");
        map.addComposited(three);
        assertTrue(map.containsKey("5"));
        try {
            map.addComposited(three);
            fail("Expecting IllegalArgumentException.");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.collections.map.TestCompositeMap::testRemoveComposited
    public void testRemoveComposited() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo());
        HashMap three = new HashMap();
        three.put("5", "five");
        map.addComposited(three);
        assertTrue(map.containsKey("5"));
        
        map.removeComposited(three);
        assertFalse(map.containsKey("5"));
        
        map.removeComposited(buildOne());
        assertFalse(map.containsKey("2"));
        
    }

// org.apache.commons.collections.map.TestCompositeMap::testRemoveFromUnderlying
    public void testRemoveFromUnderlying() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo());
        HashMap three = new HashMap();
        three.put("5", "five");
        map.addComposited(three);
        assertTrue(map.containsKey("5"));
        
        
        three.remove("5");
        assertFalse(map.containsKey("5"));
    }

// org.apache.commons.collections.map.TestCompositeMap::testRemoveFromComposited
    public void testRemoveFromComposited() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo());
        HashMap three = new HashMap();
        three.put("5", "five");
        map.addComposited(three);
        assertTrue(map.containsKey("5"));
        
        
        map.remove("5");
        assertFalse(three.containsKey("5"));
    }

// org.apache.commons.collections.map.TestCompositeMap::testResolveCollision
    public void testResolveCollision() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo(), 
            new CompositeMap.MapMutator() {
            public void resolveCollision(CompositeMap composite,
            Map existing,
            Map added,
            Collection intersect) {
                pass = true;
            }
            
            public Object put(CompositeMap map, Map[] composited, Object key, 
                Object value) {
                throw new UnsupportedOperationException();
            }
            
            public void putAll(CompositeMap map, Map[] composited, Map t) {
                throw new UnsupportedOperationException();
            }
        });
        
        map.addComposited(buildOne());
        assertTrue(pass);
    }

// org.apache.commons.collections.map.TestCompositeMap::testPut
    public void testPut() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo(), 
            new CompositeMap.MapMutator() {
            public void resolveCollision(CompositeMap composite,
            Map existing,
            Map added,
            Collection intersect) {
                throw new UnsupportedOperationException();
            }
            
            public Object put(CompositeMap map, Map[] composited, Object key, 
                Object value) {
                pass = true;
                return "foo";
            }
            
            public void putAll(CompositeMap map, Map[] composited, Map t) {
                throw new UnsupportedOperationException();
            }
        });
        
        map.put("willy", "wonka");
        assertTrue(pass);
    }

// org.apache.commons.collections.map.TestCompositeMap::testPutAll
    public void testPutAll() {
        CompositeMap map = new CompositeMap(buildOne(), buildTwo(), 
            new CompositeMap.MapMutator() {
            public void resolveCollision(CompositeMap composite,
            Map existing,
            Map added,
            Collection intersect) {
                throw new UnsupportedOperationException();
            }
            
            public Object put(CompositeMap map, Map[] composited, Object key,
                Object value) {
                throw new UnsupportedOperationException();
            }
            
            public void putAll(CompositeMap map, Map[] composited, Map t) {
                pass = true;
            }
        });
        
        map.putAll(null);
        assertTrue(pass);
    }

// org.apache.commons.collections.set.TestCompositeSet::testContains
    public void testContains() {
        CompositeSet set = new CompositeSet(new Set[]{buildOne(), buildTwo()});
        assertTrue(set.contains("1"));
    }

// org.apache.commons.collections.set.TestCompositeSet::testRemoveUnderlying
    public void testRemoveUnderlying() {
        Set one = buildOne();
        Set two = buildTwo();
        CompositeSet set = new CompositeSet(new Set[]{one, two});
        one.remove("1");
        assertFalse(set.contains("1"));
        
        two.remove("3");
        assertFalse(set.contains("3"));
    }

// org.apache.commons.collections.set.TestCompositeSet::testRemoveComposited
    public void testRemoveComposited() {
        Set one = buildOne();
        Set two = buildTwo();
        CompositeSet set = new CompositeSet(new Set[]{one, two});
        set.remove("1");
        assertFalse(one.contains("1"));
        
        set.remove("3");
        assertFalse(one.contains("3"));
    }

// org.apache.commons.collections.set.TestCompositeSet::testFailedCollisionResolution
    public void testFailedCollisionResolution() {
        Set one = buildOne();
        Set two = buildTwo();
        CompositeSet set = new CompositeSet(new Set[]{one, two});
        set.setMutator(new CompositeSet.SetMutator() {
            public void resolveCollision(CompositeSet comp, Set existing, 
                Set added, Collection intersects) {
            }
            
            public boolean add(CompositeCollection composite, 
                Collection[] collections, Object obj) {
                throw new UnsupportedOperationException();
            }
            
            public boolean addAll(CompositeCollection composite, 
                Collection[] collections, Collection coll) {
                throw new UnsupportedOperationException();
            }
            
            public boolean remove(CompositeCollection composite, 
                Collection[] collections, Object obj) {
                throw new UnsupportedOperationException();
            }
        });
        
        HashSet three = new HashSet();
        three.add("1");
        try {
            set.addComposited(three);
            fail("IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.collections.set.TestCompositeSet::testAddComposited
    public void testAddComposited() {
        Set one = buildOne();
        Set two = buildTwo();
        CompositeSet set = new CompositeSet();
        set.addComposited(one, two);
        CompositeSet set2 = new CompositeSet(buildOne());
        set2.addComposited(buildTwo());
        assertTrue(set.equals(set2));
        HashSet set3 = new HashSet();
        set3.add("1");
        set3.add("2");
        set3.add("3");
        HashSet set4 = new HashSet();
        set4.add("4");
        CompositeSet set5 = new CompositeSet(set3);
        set5.addComposited(set4);
        assertTrue(set.equals(set5));
        try {
            set.addComposited(set3);
            fail("Expecting UnsupportedOperationException.");
        } catch (UnsupportedOperationException ex) {
            
        }
    }
