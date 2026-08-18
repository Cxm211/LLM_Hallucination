// buggy code
    public int compareTo(Object other) {
        return iValue - ((ValuedEnum) other).iValue;
    }

// relevant test
// org.apache.commons.lang.enums.EnumEqualsTest::testEquals
    public void testEquals() {
        assertEquals(false, CarColorEnum.RED.equals(TrafficlightColorEnum.RED));
        assertEquals(false, CarColorEnum.YELLOW.equals(TrafficlightColorEnum.YELLOW));
        
        assertEquals(false, TrafficlightColorEnum.RED.equals(new TotallyUnrelatedClass("red")));
        assertEquals(false, CarColorEnum.RED.equals(new TotallyUnrelatedClass("red")));
        
        assertEquals(false, TrafficlightColorEnum.RED.equals(new TotallyUnrelatedClass("some")));
        assertEquals(false, CarColorEnum.RED.equals(new TotallyUnrelatedClass("some")));
    }

// org.apache.commons.lang.enums.EnumEqualsTest::testEquals_classloader_equal
    public void testEquals_classloader_equal() throws Exception {
        ClassLoader cl = ColorEnum.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader urlCL = (URLClassLoader) cl;
            URLClassLoader urlCL1 = new URLClassLoader(urlCL.getURLs(), null);
            URLClassLoader urlCL2 = new URLClassLoader(urlCL.getURLs(), null);
            Class otherEnumClass1 = urlCL1.loadClass("org.apache.commons.lang.enums.ColorEnum");
            Class otherEnumClass2 = urlCL2.loadClass("org.apache.commons.lang.enums.ColorEnum");
            Object blue1 = otherEnumClass1.getDeclaredField("BLUE").get(null);
            Object blue2 = otherEnumClass2.getDeclaredField("BLUE").get(null);
            assertEquals(true, blue1.equals(blue2));
        }
    }

// org.apache.commons.lang.enums.EnumEqualsTest::testEquals_classloader_different
    public void testEquals_classloader_different() throws Exception {
        ClassLoader cl = ColorEnum.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader urlCL = (URLClassLoader) cl;
            URLClassLoader urlCL1 = new URLClassLoader(urlCL.getURLs(), null);
            URLClassLoader urlCL2 = new URLClassLoader(urlCL.getURLs(), null);
            Class otherEnumClass1 = urlCL1.loadClass("org.apache.commons.lang.enums.ColorEnum");
            Class otherEnumClass2 = urlCL2.loadClass("org.apache.commons.lang.enums.ColorEnum");
            Object blue1 = otherEnumClass1.getDeclaredField("BLUE").get(null);
            Object blue2 = otherEnumClass2.getDeclaredField("RED").get(null);
            assertEquals(false, blue1.equals(blue2));
        }
    }

// org.apache.commons.lang.enums.EnumEqualsTest::testCompareTo
    public void testCompareTo() {
        try {
            CarColorEnum.RED.compareTo(TrafficlightColorEnum.RED);
            fail();
        } catch (ClassCastException ex) {}
        try {
            CarColorEnum.YELLOW.compareTo(TrafficlightColorEnum.YELLOW);
            fail();
        } catch (ClassCastException ex) {}
        try {
            TrafficlightColorEnum.RED.compareTo(new TotallyUnrelatedClass("red"));
            fail();
        } catch (ClassCastException ex) {}
        try {
            CarColorEnum.RED.compareTo(new TotallyUnrelatedClass("red"));
            fail();
        } catch (ClassCastException ex) {}
        try {
            TrafficlightColorEnum.RED.compareTo(new TotallyUnrelatedClass("some"));
            fail();
        } catch (ClassCastException ex) {}
        try {
            CarColorEnum.RED.compareTo(new TotallyUnrelatedClass("some"));
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.enums.EnumTest::testName
    public void testName() {
        assertEquals("Red", ColorEnum.RED.getName());
        assertEquals("Green", ColorEnum.GREEN.getName());
        assertEquals("Blue", ColorEnum.BLUE.getName());
    }

// org.apache.commons.lang.enums.EnumTest::testCompareTo
    public void testCompareTo() {
        assertTrue(ColorEnum.BLUE.compareTo(ColorEnum.BLUE) == 0);
        assertTrue(ColorEnum.RED.compareTo(ColorEnum.BLUE) > 0);
        assertTrue(ColorEnum.BLUE.compareTo(ColorEnum.RED) < 0);
        try {
            ColorEnum.RED.compareTo(null);
            fail();
        } catch (NullPointerException ex) {}
        try {
            ColorEnum.RED.compareTo(new Object());
            fail();
        } catch (ClassCastException ex) {}
    }

// org.apache.commons.lang.enums.EnumTest::testEquals
    public void testEquals() {
        assertSame(ColorEnum.RED, ColorEnum.RED);
        assertSame(ColorEnum.getEnum("Red"), ColorEnum.RED);
        assertEquals(false, ColorEnum.RED.equals(null));
        assertEquals(true, ColorEnum.RED.equals(ColorEnum.RED));
        assertEquals(true, ColorEnum.RED.equals(ColorEnum.getEnum("Red")));
    }

// org.apache.commons.lang.enums.EnumTest::testHashCode
    public void testHashCode() {
        assertEquals(ColorEnum.RED.hashCode(), ColorEnum.RED.hashCode());
        assertEquals(7 + ColorEnum.class.hashCode() + 3 * "Red".hashCode(), ColorEnum.RED.hashCode());
    }

// org.apache.commons.lang.enums.EnumTest::testToString
    public void testToString() {
        String toString = ColorEnum.RED.toString();
        assertEquals("ColorEnum[Red]", toString);
        assertSame(toString, ColorEnum.RED.toString());
    }

// org.apache.commons.lang.enums.EnumTest::testIterator
    public void testIterator() {
        Iterator it = ColorEnum.iterator();
        assertSame(ColorEnum.RED, it.next());
        assertSame(ColorEnum.GREEN, it.next());
        assertSame(ColorEnum.BLUE, it.next());
    }

// org.apache.commons.lang.enums.EnumTest::testList
    public void testList() {
        List list = new ArrayList(ColorEnum.getEnumList());
        
        assertNotNull(list);
        
        assertEquals( list.size(),
                        ColorEnum.getEnumMap().keySet().size());
        
        Iterator it = list.iterator();
        assertSame(ColorEnum.RED, it.next());
        assertSame(ColorEnum.GREEN, it.next());
        assertSame(ColorEnum.BLUE, it.next());
    }

// org.apache.commons.lang.enums.EnumTest::testMap
    public void testMap() {
        Map map = new HashMap(ColorEnum.getEnumMap());
        
        assertNotNull(map);
        assertTrue(map.containsValue(ColorEnum.RED));
        assertTrue(map.containsValue(ColorEnum.GREEN));
        assertTrue(map.containsValue(ColorEnum.BLUE));
        assertSame(ColorEnum.RED, map.get("Red"));
        assertSame(ColorEnum.GREEN, map.get("Green"));
        assertSame(ColorEnum.BLUE, map.get("Blue"));
        assertEquals( map.keySet().size(),
                        ColorEnum.getEnumList().size());
    }

// org.apache.commons.lang.enums.EnumTest::testGet
    public void testGet() {
        assertSame(ColorEnum.RED, ColorEnum.getEnum("Red"));
        assertSame(ColorEnum.GREEN, ColorEnum.getEnum("Green"));
        assertSame(ColorEnum.BLUE, ColorEnum.getEnum("Blue"));
        assertSame(null, ColorEnum.getEnum("Pink"));
    }

// org.apache.commons.lang.enums.EnumTest::testSerialization
    public void testSerialization() {
        int hashCode = ColorEnum.RED.hashCode();
        assertSame(ColorEnum.RED, SerializationUtils.clone(ColorEnum.RED));
        assertEquals(hashCode, SerializationUtils.clone(ColorEnum.RED).hashCode());
        assertSame(ColorEnum.GREEN, SerializationUtils.clone(ColorEnum.GREEN));
        assertSame(ColorEnum.BLUE, SerializationUtils.clone(ColorEnum.BLUE));
    }

// org.apache.commons.lang.enums.EnumTest::testBroken1
    public void testBroken1() {
        try {
            Broken1Enum.RED.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testBroken2
    public void testBroken2() {
        try {
            Broken2Enum.RED.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testBroken3
    public void testBroken3() {
        try {
            Broken3Enum.RED.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testBroken1Operation
    public void testBroken1Operation() {
        try {
            Broken1OperationEnum.PLUS.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testBroken2Operation
    public void testBroken2Operation() {
        try {
            Broken2OperationEnum.PLUS.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testBroken3Operation
    public void testBroken3Operation() {
        try {
            Broken3OperationEnum.PLUS.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testBroken4Operation
    public void testBroken4Operation() {
        try {
            Broken4OperationEnum.PLUS.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testBroken5Operation
    public void testBroken5Operation() {
        try {
            Broken5OperationEnum.PLUS.getName();
            fail();
        } catch (ExceptionInInitializerError ex) {
            assertTrue(ex.getException() instanceof IllegalArgumentException);
        }
    }

// org.apache.commons.lang.enums.EnumTest::testOperationGet
    public void testOperationGet() {
        assertSame(OperationEnum.PLUS, OperationEnum.getEnum("Plus"));
        assertSame(OperationEnum.MINUS, OperationEnum.getEnum("Minus"));
        assertSame(null, OperationEnum.getEnum("Pink"));
    }

// org.apache.commons.lang.enums.EnumTest::testOperationSerialization
    public void testOperationSerialization() {
        assertSame(OperationEnum.PLUS, SerializationUtils.clone(OperationEnum.PLUS));
        assertSame(OperationEnum.MINUS, SerializationUtils.clone(OperationEnum.MINUS));
    }

// org.apache.commons.lang.enums.EnumTest::testOperationToString
    public void testOperationToString() {
        assertEquals("OperationEnum[Plus]", OperationEnum.PLUS.toString());
    }

// org.apache.commons.lang.enums.EnumTest::testOperationList
    public void testOperationList() {
        List list = OperationEnum.getEnumList();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(list.size(), OperationEnum.getEnumMap().keySet().size());
        
        Iterator it = list.iterator();
        assertSame(OperationEnum.PLUS, it.next());
        assertSame(OperationEnum.MINUS, it.next());
    }

// org.apache.commons.lang.enums.EnumTest::testOperationMap
    public void testOperationMap() {
        Map map = OperationEnum.getEnumMap();
        assertNotNull(map);
        assertEquals(map.keySet().size(), OperationEnum.getEnumList().size());
        
        assertTrue(map.containsValue(OperationEnum.PLUS));
        assertTrue(map.containsValue(OperationEnum.MINUS));
        assertSame(OperationEnum.PLUS, map.get("Plus"));
        assertSame(OperationEnum.MINUS, map.get("Minus"));
    }

// org.apache.commons.lang.enums.EnumTest::testOperationCalculation
    public void testOperationCalculation() {
        assertEquals(3, OperationEnum.PLUS.eval(1, 2));
        assertEquals(-1, OperationEnum.MINUS.eval(1, 2));
    }

// org.apache.commons.lang.enums.EnumTest::testExtended1Get
    public void testExtended1Get() {
        assertSame(Extended1Enum.ALPHA, Extended1Enum.getEnum("Alpha"));
        assertSame(Extended1Enum.BETA, Extended1Enum.getEnum("Beta"));
        assertSame(null, Extended1Enum.getEnum("Gamma"));
        assertSame(null, Extended1Enum.getEnum("Delta"));
    }

// org.apache.commons.lang.enums.EnumTest::testExtended2Get
    public void testExtended2Get() {
        assertSame(Extended1Enum.ALPHA, Extended2Enum.ALPHA);
        assertSame(Extended1Enum.BETA, Extended2Enum.BETA);
        
        assertSame(Extended2Enum.ALPHA, Extended2Enum.getEnum("Alpha"));
        assertSame(Extended2Enum.BETA, Extended2Enum.getEnum("Beta"));
        assertSame(Extended2Enum.GAMMA, Extended2Enum.getEnum("Gamma"));
        assertSame(null, Extended2Enum.getEnum("Delta"));
    }

// org.apache.commons.lang.enums.EnumTest::testExtended3Get
    public void testExtended3Get() {
        assertSame(Extended2Enum.ALPHA, Extended3Enum.ALPHA);
        assertSame(Extended2Enum.BETA, Extended3Enum.BETA);
        assertSame(Extended2Enum.GAMMA, Extended3Enum.GAMMA);
        
        assertSame(Extended3Enum.ALPHA, Extended3Enum.getEnum("Alpha"));
        assertSame(Extended3Enum.BETA, Extended3Enum.getEnum("Beta"));
        assertSame(Extended3Enum.GAMMA, Extended3Enum.getEnum("Gamma"));
        assertSame(Extended3Enum.DELTA, Extended3Enum.getEnum("Delta"));
    }

// org.apache.commons.lang.enums.EnumTest::testExtendedSerialization
    public void testExtendedSerialization() {
        assertSame(Extended1Enum.ALPHA, SerializationUtils.clone(Extended1Enum.ALPHA));
        assertSame(Extended1Enum.BETA, SerializationUtils.clone(Extended1Enum.BETA));
        assertSame(Extended2Enum.GAMMA, SerializationUtils.clone(Extended2Enum.GAMMA));
        assertSame(Extended3Enum.DELTA, SerializationUtils.clone(Extended3Enum.DELTA));
    }

// org.apache.commons.lang.enums.EnumTest::testExtendedToString
    public void testExtendedToString() {
        assertEquals("Extended1Enum[Alpha]", Extended1Enum.ALPHA.toString());
        assertEquals("Extended1Enum[Beta]", Extended1Enum.BETA.toString());
        
        assertEquals("Extended1Enum[Alpha]", Extended2Enum.ALPHA.toString());
        assertEquals("Extended1Enum[Beta]", Extended2Enum.BETA.toString());
        assertEquals("Extended2Enum[Gamma]", Extended2Enum.GAMMA.toString());
        
        assertEquals("Extended1Enum[Alpha]", Extended3Enum.ALPHA.toString());
        assertEquals("Extended1Enum[Beta]", Extended3Enum.BETA.toString());
        assertEquals("Extended2Enum[Gamma]", Extended3Enum.GAMMA.toString());
        assertEquals("Extended3Enum[Delta]", Extended3Enum.DELTA.toString());
    }

// org.apache.commons.lang.enums.EnumTest::testExtended1List
    public void testExtended1List() {
        List list = Extended1Enum.getEnumList();
        assertNotNull(list);
        assertEquals(2, list.size());
        assertEquals(list.size(), Extended1Enum.getEnumMap().keySet().size());
        
        Iterator it = list.iterator();
        assertSame(Extended1Enum.ALPHA, it.next());
        assertSame(Extended1Enum.BETA, it.next());
    }

// org.apache.commons.lang.enums.EnumTest::testExtended2List
    public void testExtended2List() {
        List list = Extended2Enum.getEnumList();
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals(list.size(), Extended2Enum.getEnumMap().keySet().size());
        
        Iterator it = list.iterator();
        assertSame(Extended2Enum.ALPHA, it.next());
        assertSame(Extended2Enum.BETA, it.next());
        assertSame(Extended2Enum.GAMMA, it.next());
    }

// org.apache.commons.lang.enums.EnumTest::testExtended3List
    public void testExtended3List() {
        List list = Extended3Enum.getEnumList();
        assertNotNull(list);
        assertEquals(4, list.size());
        assertEquals(list.size(), Extended3Enum.getEnumMap().keySet().size());
        
        Iterator it = list.iterator();
        assertSame(Extended3Enum.ALPHA, it.next());
        assertSame(Extended3Enum.BETA, it.next());
        assertSame(Extended3Enum.GAMMA, it.next());
        assertSame(Extended3Enum.DELTA, it.next());
    }

// org.apache.commons.lang.enums.EnumTest::testExtended1Map
    public void testExtended1Map() {
        Map map = Extended1Enum.getEnumMap();
        assertNotNull(map);
        assertEquals(map.keySet().size(), Extended1Enum.getEnumList().size());
        
        assertTrue(map.containsValue(Extended1Enum.ALPHA));
        assertTrue(map.containsValue(Extended1Enum.BETA));
        assertSame(Extended1Enum.ALPHA, map.get("Alpha"));
        assertSame(Extended1Enum.BETA, map.get("Beta"));
    }

// org.apache.commons.lang.enums.EnumTest::testExtended2Map
    public void testExtended2Map() {
        Map map = Extended2Enum.getEnumMap();
        assertNotNull(map);
        assertEquals(map.keySet().size(), Extended2Enum.getEnumList().size());
        
        assertTrue(map.containsValue(Extended2Enum.ALPHA));
        assertTrue(map.containsValue(Extended2Enum.BETA));
        assertTrue(map.containsValue(Extended2Enum.GAMMA));
        assertSame(Extended2Enum.ALPHA, map.get("Alpha"));
        assertSame(Extended2Enum.BETA, map.get("Beta"));
        assertSame(Extended2Enum.GAMMA, map.get("Gamma"));
    }

// org.apache.commons.lang.enums.EnumTest::testExtended3Map
    public void testExtended3Map() {
        Map map = Extended3Enum.getEnumMap();
        assertNotNull(map);
        assertEquals(map.keySet().size(), Extended3Enum.getEnumList().size());
        
        assertTrue(map.containsValue(Extended3Enum.ALPHA));
        assertTrue(map.containsValue(Extended3Enum.BETA));
        assertTrue(map.containsValue(Extended3Enum.GAMMA));
        assertTrue(map.containsValue(Extended3Enum.DELTA));
        assertSame(Extended3Enum.ALPHA, map.get("Alpha"));
        assertSame(Extended3Enum.BETA, map.get("Beta"));
        assertSame(Extended3Enum.GAMMA, map.get("Gamma"));
        assertSame(Extended3Enum.DELTA, map.get("Delta"));
    }

// org.apache.commons.lang.enums.EnumTest::testNested
    public void testNested() {
        List list = new ArrayList(Nest.ColorEnum.getEnumList());
        assertEquals(3, list.size());  
        Iterator it = list.iterator();
        assertSame(Nest.ColorEnum.RED, it.next());
        assertSame(Nest.ColorEnum.GREEN, it.next());
        assertSame(Nest.ColorEnum.BLUE, it.next());
        
        
    }

// org.apache.commons.lang.enums.EnumTest::testNestedBroken
    public void testNestedBroken() {
        List list = new ArrayList(NestBroken.ColorEnum.getEnumList());
        try {
            assertEquals(0, list.size());  
            
            
            
        } catch (AssertionFailedError ex) {
            
            assertEquals(3, list.size());
        }
        new NestBroken();
        list = new ArrayList(NestBroken.ColorEnum.getEnumList());
        assertEquals(3, list.size());  
        Iterator it = list.iterator();
        assertSame(NestBroken.RED, it.next());
        assertSame(NestBroken.GREEN, it.next());
        assertSame(NestBroken.BLUE, it.next());
    }

// org.apache.commons.lang.enums.EnumTest::testNestedLinked
    public void testNestedLinked() {
        List list = new ArrayList(NestLinked.ColorEnum.getEnumList());
        assertEquals(3, list.size());  
        Iterator it = list.iterator();
        assertSame(NestLinked.RED, it.next());
        assertSame(NestLinked.GREEN, it.next());
        assertSame(NestLinked.BLUE, it.next());
        
        
    }

// org.apache.commons.lang.enums.EnumTest::testNestedReferenced
    public void testNestedReferenced() {
        List list = new ArrayList(NestReferenced.ColorEnum.getEnumList());
        assertEquals(3, list.size());  
        Iterator it = list.iterator();
        assertSame(NestReferenced.RED, it.next());
        assertSame(NestReferenced.GREEN, it.next());
        assertSame(NestReferenced.BLUE, it.next());
        
        
        
    }

// org.apache.commons.lang.enums.EnumTest::testColorEnumEqualsWithDifferentClassLoaders
    public void testColorEnumEqualsWithDifferentClassLoaders() {}

// org.apache.commons.lang.enums.EnumTest::testEqualsToWrongInstance
    public void testEqualsToWrongInstance() {
        for (Iterator iter = ColorEnum.iterator(); iter.hasNext();) {
            ColorEnum element = (ColorEnum) iter.next();
            this.testEqualsToWrongInstance(element);
        }
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testConstructor
    public void testConstructor() {
        assertNotNull(new EnumUtils());
        Constructor[] cons = EnumUtils.class.getDeclaredConstructors();
        assertEquals(1, cons.length);
        assertEquals(true, Modifier.isPublic(cons[0].getModifiers()));
        assertEquals(true, Modifier.isPublic(EnumUtils.class.getModifiers()));
        assertEquals(false, Modifier.isFinal(EnumUtils.class.getModifiers()));
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testIterator
    public void testIterator() {
        Iterator it = EnumUtils.iterator(ColorEnum.class);
        assertSame(ColorEnum.RED, it.next());
        assertSame(ColorEnum.GREEN, it.next());
        assertSame(ColorEnum.BLUE, it.next());
        it = EnumUtils.iterator(DummyEnum.class);
        assertEquals(false, it.hasNext());
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testIteratorEx
    public void testIteratorEx() {
        try {
            EnumUtils.iterator(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            EnumUtils.iterator(Object.class);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testList
    public void testList() {
        List list = EnumUtils.getEnumList(ColorEnum.class);
        Iterator it = list.iterator();
        assertSame(ColorEnum.RED, it.next());
        assertSame(ColorEnum.GREEN, it.next());
        assertSame(ColorEnum.BLUE, it.next());
        list = EnumUtils.getEnumList(DummyEnum.class);
        assertEquals(0, list.size());
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testListEx
    public void testListEx() {
        try {
            EnumUtils.getEnumList(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            EnumUtils.getEnumList(Object.class);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testMap
    public void testMap() {
        Map map = EnumUtils.getEnumMap(ColorEnum.class);
        assertTrue(map.containsValue(ColorEnum.RED));
        assertTrue(map.containsValue(ColorEnum.GREEN));
        assertTrue(map.containsValue(ColorEnum.BLUE));
        assertSame(ColorEnum.RED, map.get("Red"));
        assertSame(ColorEnum.GREEN, map.get("Green"));
        assertSame(ColorEnum.BLUE, map.get("Blue"));
        map = EnumUtils.getEnumMap(DummyEnum.class);
        assertEquals(0, map.size());
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testMapEx
    public void testMapEx() {
        try {
            EnumUtils.getEnumMap(null);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            EnumUtils.getEnumMap(Object.class);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testGet
    public void testGet() {
        assertSame(ColorEnum.RED, EnumUtils.getEnum(ColorEnum.class, "Red"));
        assertSame(ColorEnum.GREEN, EnumUtils.getEnum(ColorEnum.class, "Green"));
        assertSame(ColorEnum.BLUE, EnumUtils.getEnum(ColorEnum.class, "Blue"));
        assertSame(null, EnumUtils.getEnum(ColorEnum.class, "Pink"));
        assertSame(null, EnumUtils.getEnum(DummyEnum.class, "Pink"));
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testGetEx
    public void testGetEx() {
        try {
            EnumUtils.getEnum(null, "");
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            EnumUtils.getEnum(Object.class, "Red");
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testGetValue
    public void testGetValue() {
        assertSame(ValuedColorEnum.RED, EnumUtils.getEnum(ValuedColorEnum.class, 1));
        assertSame(ValuedColorEnum.GREEN, EnumUtils.getEnum(ValuedColorEnum.class, 2));
        assertSame(ValuedColorEnum.BLUE, EnumUtils.getEnum(ValuedColorEnum.class, 3));
        assertSame(null, EnumUtils.getEnum(ValuedColorEnum.class, 4));
        assertSame(null, EnumUtils.getEnum(DummyEnum.class, 5));
    }

// org.apache.commons.lang.enums.EnumUtilsTest::testGetValueEx
    public void testGetValueEx() {
        try {
            EnumUtils.getEnum(null, 0);
            fail();
        } catch (IllegalArgumentException ex) {}
        try {
            EnumUtils.getEnum(Object.class, 2);
            fail();
        } catch (IllegalArgumentException ex) {}
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testName
    public void testName() {
        assertEquals("Red", ValuedColorEnum.RED.getName());
        assertEquals("Green", ValuedColorEnum.GREEN.getName());
        assertEquals("Blue", ValuedColorEnum.BLUE.getName());
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testValue
    public void testValue() {
        assertEquals(1, ValuedColorEnum.RED.getValue());
        assertEquals(2, ValuedColorEnum.GREEN.getValue());
        assertEquals(3, ValuedColorEnum.BLUE.getValue());
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testCompareTo
    public void testCompareTo() {
        assertTrue(ValuedColorEnum.BLUE.compareTo(ValuedColorEnum.BLUE) == 0);
        assertTrue(ValuedColorEnum.RED.compareTo(ValuedColorEnum.BLUE) < 0);
        assertTrue(ValuedColorEnum.BLUE.compareTo(ValuedColorEnum.RED) > 0);
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testCompareTo_classloader_equal
    public void testCompareTo_classloader_equal() throws Exception {
        ClassLoader cl = ValuedColorEnum.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader urlCL = (URLClassLoader) cl;
            URLClassLoader urlCL1 = new URLClassLoader(urlCL.getURLs(), null);
            URLClassLoader urlCL2 = new URLClassLoader(urlCL.getURLs(), null);
            Class otherEnumClass1 = urlCL1.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Class otherEnumClass2 = urlCL2.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Object blue1 = otherEnumClass1.getDeclaredField("BLUE").get(null);
            Object blue2 = otherEnumClass2.getDeclaredField("BLUE").get(null);
            assertTrue(((Comparable) blue1).compareTo(blue2) == 0);
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testCompareTo_classloader_different
    public void testCompareTo_classloader_different() throws Exception {
        ClassLoader cl = ValuedColorEnum.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader urlCL = (URLClassLoader) cl;
            URLClassLoader urlCL1 = new URLClassLoader(urlCL.getURLs(), null);
            URLClassLoader urlCL2 = new URLClassLoader(urlCL.getURLs(), null);
            Class otherEnumClass1 = urlCL1.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Class otherEnumClass2 = urlCL2.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Object blue1 = otherEnumClass1.getDeclaredField("BLUE").get(null);
            Object blue2 = otherEnumClass2.getDeclaredField("RED").get(null);
            assertTrue(((Comparable) blue1).compareTo(blue2) != 0);
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testCompareTo_nonEnumType
    public void testCompareTo_nonEnumType() {
        try {
            ValuedColorEnum.BLUE.compareTo(new TotallyUnrelatedClass(ValuedColorEnum.BLUE.getValue()));
            fail();
        } catch (ClassCastException ex) {
            
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testCompareTo_otherEnumType
    public void testCompareTo_otherEnumType() {
        try {
            ValuedColorEnum.BLUE.compareTo(ValuedLanguageEnum.ENGLISH);
            fail();
        } catch (ClassCastException ex) {
            
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testCompareTo_otherType
    public void testCompareTo_otherType() {
        try {
            ValuedColorEnum.BLUE.compareTo("Blue");
            fail();
        } catch (ClassCastException ex) {
            
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testCompareTo_null
    public void testCompareTo_null() {
        try {
            ValuedColorEnum.BLUE.compareTo(null);
            fail();
        } catch (NullPointerException ex) {
            
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testEquals
    public void testEquals() {
        assertSame(ValuedColorEnum.RED, ValuedColorEnum.RED);
        assertSame(ValuedColorEnum.getEnum("Red"), ValuedColorEnum.RED);
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testEquals_classloader_equal
    public void testEquals_classloader_equal() throws Exception {
        ClassLoader cl = ValuedColorEnum.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader urlCL = (URLClassLoader) cl;
            URLClassLoader urlCL1 = new URLClassLoader(urlCL.getURLs(), null);
            URLClassLoader urlCL2 = new URLClassLoader(urlCL.getURLs(), null);
            Class otherEnumClass1 = urlCL1.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Class otherEnumClass2 = urlCL2.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Object blue1 = otherEnumClass1.getDeclaredField("BLUE").get(null);
            Object blue2 = otherEnumClass2.getDeclaredField("BLUE").get(null);
            assertEquals(true, blue1.equals(blue2));
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testEquals_classloader_different
    public void testEquals_classloader_different() throws Exception {
        ClassLoader cl = ValuedColorEnum.class.getClassLoader();
        if (cl instanceof URLClassLoader) {
            URLClassLoader urlCL = (URLClassLoader) cl;
            URLClassLoader urlCL1 = new URLClassLoader(urlCL.getURLs(), null);
            URLClassLoader urlCL2 = new URLClassLoader(urlCL.getURLs(), null);
            Class otherEnumClass1 = urlCL1.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Class otherEnumClass2 = urlCL2.loadClass("org.apache.commons.lang.enums.ValuedColorEnum");
            Object blue1 = otherEnumClass1.getDeclaredField("BLUE").get(null);
            Object blue2 = otherEnumClass2.getDeclaredField("RED").get(null);
            assertEquals(false, blue1.equals(blue2));
        }
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testToString
    public void testToString() {
        String toString = ValuedColorEnum.RED.toString();
        assertEquals("ValuedColorEnum[Red=1]", toString);
        assertSame(toString, ValuedColorEnum.RED.toString());
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testIterator
    public void testIterator() {
        Iterator it = ValuedColorEnum.iterator();
        assertSame(ValuedColorEnum.RED, it.next());
        assertSame(ValuedColorEnum.GREEN, it.next());
        assertSame(ValuedColorEnum.BLUE, it.next());
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testList
    public void testList() {
        List list = ValuedColorEnum.getEnumList();
        
        assertNotNull(list);
        
        assertEquals( list.size(),
                     ValuedColorEnum.getEnumMap().keySet().size());
        
        Iterator it = list.iterator();
        assertSame(ValuedColorEnum.RED, it.next());
        assertSame(ValuedColorEnum.GREEN, it.next());
        assertSame(ValuedColorEnum.BLUE, it.next());
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testMap
    public void testMap() {
        Map map = ValuedColorEnum.getEnumMap();
        
        assertNotNull(map);
        
        assertEquals( map.keySet().size(),
                     ValuedColorEnum.getEnumList().size());
                     
        assertTrue(map.containsValue(ValuedColorEnum.RED));
        assertTrue(map.containsValue(ValuedColorEnum.GREEN));
        assertTrue(map.containsValue(ValuedColorEnum.BLUE));
        assertSame(ValuedColorEnum.RED, map.get("Red"));
        assertSame(ValuedColorEnum.GREEN, map.get("Green"));
        assertSame(ValuedColorEnum.BLUE, map.get("Blue"));
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testGet
    public void testGet() {
        assertSame(ValuedColorEnum.RED, ValuedColorEnum.getEnum("Red"));
        assertSame(ValuedColorEnum.GREEN, ValuedColorEnum.getEnum("Green"));
        assertSame(ValuedColorEnum.BLUE, ValuedColorEnum.getEnum("Blue"));
        assertSame(null, ValuedColorEnum.getEnum("Pink"));
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testGetValue
    public void testGetValue() {
        assertSame(ValuedColorEnum.RED, ValuedColorEnum.getEnum(1));
        assertSame(ValuedColorEnum.GREEN, ValuedColorEnum.getEnum(2));
        assertSame(ValuedColorEnum.BLUE, ValuedColorEnum.getEnum(3));
        assertSame(null, ValuedColorEnum.getEnum(4));
    }

// org.apache.commons.lang.enums.ValuedEnumTest::testSerialization
    public void testSerialization() {
        assertSame(ValuedColorEnum.RED, SerializationUtils.clone(ValuedColorEnum.RED));
        assertSame(ValuedColorEnum.GREEN, SerializationUtils.clone(ValuedColorEnum.GREEN));
        assertSame(ValuedColorEnum.BLUE, SerializationUtils.clone(ValuedColorEnum.BLUE));
    }
