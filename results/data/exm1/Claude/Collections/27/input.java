// buggy code
        public T create() {
            try {
                return clazz.newInstance();
            } catch (final Exception ex) {
                throw new FunctorException("Cannot instantiate class: " + clazz, ex);
            }
        }

// relevant test
// org.apache.commons.collections4.MapUtilsTest::testPredicatedMap
    public void testPredicatedMap() {
        final Predicate<Object> p = getPredicate();
        Map<Object, Object> map = MapUtils.predicatedMap(new HashMap<Object, Object>(), p, p);
        assertTrue("returned object should be a PredicatedMap", map instanceof PredicatedMap);
        try {
            MapUtils.predicatedMap(null, p, p);
            fail("Expecting NullPointerException for null map.");
        } catch (final NullPointerException e) {
            
        }
    }

// org.apache.commons.collections4.MapUtilsTest::testLazyMapFactory
    public void testLazyMapFactory() {
        final Factory<Integer> factory = FactoryUtils.constantFactory(Integer.valueOf(5));
        Map<Object, Object> map = MapUtils.lazyMap(new HashMap<Object, Object>(), factory);
        assertTrue(map instanceof LazyMap);
        try {
            map = MapUtils.lazyMap(new HashMap<Object, Object>(), (Factory<Object>) null);
            fail("Expecting NullPointerException for null factory");
        } catch (final NullPointerException e) {
            
        }
        try {
            map = MapUtils.lazyMap((Map<Object, Object>) null, factory);
            fail("Expecting NullPointerException for null map");
        } catch (final NullPointerException e) {
            
        }
        final Transformer<Object, Integer> transformer = TransformerUtils.asTransformer(factory);
        map = MapUtils.lazyMap(new HashMap<Object, Object>(), transformer);
        assertTrue(map instanceof LazyMap);
        try {
            map = MapUtils.lazyMap(new HashMap<Object, Object>(), (Transformer<Object, Object>) null);
            fail("Expecting NullPointerException for null transformer");
        } catch (final NullPointerException e) {
            
        }
        try {
            map = MapUtils.lazyMap((Map<Object, Object>) null, transformer);
            fail("Expecting NullPointerException for null map");
        } catch (final NullPointerException e) {
            
        }
    }

// org.apache.commons.collections4.MapUtilsTest::testLazyMapTransformer
    public void testLazyMapTransformer() {
        final Map<Object, Object> map = MapUtils.lazyMap(new HashMap<Object, Object>(), new Transformer<Object, Object>() {
            public Object transform(final Object mapKey) {
                if (mapKey instanceof String) {
                    return Integer.valueOf((String) mapKey);
                }
                return null;
            }
        });

        assertEquals(0, map.size());
        final Integer i1 = (Integer) map.get("5");
        assertEquals(Integer.valueOf(5), i1);
        assertEquals(1, map.size());
        final Integer i2 = (Integer) map.get(new String(new char[] {'5'}));
        assertEquals(Integer.valueOf(5), i2);
        assertEquals(1, map.size());
        assertSame(i1, i2);
    }

// org.apache.commons.collections4.MapUtilsTest::testInvertMap
    public void testInvertMap() {
        final Map<String, String> in = new HashMap<String, String>(5, 1);
        in.put("1", "A");
        in.put("2", "B");
        in.put("3", "C");
        in.put("4", "D");
        in.put("5", "E");

        final Set<String> inKeySet = new HashSet<String>(in.keySet());
        final Set<String> inValSet = new HashSet<String>(in.values());

        final Map<String, String> out =  MapUtils.invertMap(in);

        final Set<String> outKeySet = new HashSet<String>(out.keySet());
        final Set<String> outValSet = new HashSet<String>(out.values());

        assertTrue( inKeySet.equals( outValSet ));
        assertTrue( inValSet.equals( outKeySet ));

        assertEquals( "1", out.get("A"));
        assertEquals( "2", out.get("B"));
        assertEquals( "3", out.get("C"));
        assertEquals( "4", out.get("D"));
        assertEquals( "5", out.get("E"));
    }

// org.apache.commons.collections4.MapUtilsTest::testPutAll_Map_array
    public void testPutAll_Map_array() {
        try {
            MapUtils.putAll(null, null);
            fail();
        } catch (final NullPointerException ex) {}
        try {
            MapUtils.putAll(null, new Object[0]);
            fail();
        } catch (final NullPointerException ex) {}

        Map<String, String> test = MapUtils.putAll(new HashMap<String, String>(), new String[0]);
        assertEquals(0, test.size());

        
        test = MapUtils.putAll(new HashMap<String, String>(), new String[][] {
            {"RED", "#FF0000"},
            {"GREEN", "#00FF00"},
            {"BLUE", "#0000FF"}
        });
        assertEquals(true, test.containsKey("RED"));
        assertEquals("#FF0000", test.get("RED"));
        assertEquals(true, test.containsKey("GREEN"));
        assertEquals("#00FF00", test.get("GREEN"));
        assertEquals(true, test.containsKey("BLUE"));
        assertEquals("#0000FF", test.get("BLUE"));
        assertEquals(3, test.size());

        try {
            MapUtils.putAll(new HashMap<String, String>(), new String[][] {
                {"RED", "#FF0000"},
                null,
                {"BLUE", "#0000FF"}
            });
            fail();
        } catch (final IllegalArgumentException ex) {}

        try {
            MapUtils.putAll(new HashMap<String, String>(), new String[][] {
                {"RED", "#FF0000"},
                {"GREEN"},
                {"BLUE", "#0000FF"}
            });
            fail();
        } catch (final IllegalArgumentException ex) {}

        try {
            MapUtils.putAll(new HashMap<String, String>(), new String[][] {
                {"RED", "#FF0000"},
                {},
                {"BLUE", "#0000FF"}
            });
            fail();
        } catch (final IllegalArgumentException ex) {}

        
        test = MapUtils.putAll(new HashMap<String, String>(), new String[] {
            "RED", "#FF0000",
            "GREEN", "#00FF00",
            "BLUE", "#0000FF"
        });
        assertEquals(true, test.containsKey("RED"));
        assertEquals("#FF0000", test.get("RED"));
        assertEquals(true, test.containsKey("GREEN"));
        assertEquals("#00FF00", test.get("GREEN"));
        assertEquals(true, test.containsKey("BLUE"));
        assertEquals("#0000FF", test.get("BLUE"));
        assertEquals(3, test.size());

        test = MapUtils.putAll(new HashMap<String, String>(), new String[] {
            "RED", "#FF0000",
            "GREEN", "#00FF00",
            "BLUE", "#0000FF",
            "PURPLE" 
        });
        assertEquals(true, test.containsKey("RED"));
        assertEquals("#FF0000", test.get("RED"));
        assertEquals(true, test.containsKey("GREEN"));
        assertEquals("#00FF00", test.get("GREEN"));
        assertEquals(true, test.containsKey("BLUE"));
        assertEquals("#0000FF", test.get("BLUE"));
        assertEquals(3, test.size());

        
        test = MapUtils.putAll(new HashMap<String, String>(), new Object[] {
            new DefaultMapEntry<String, String>("RED", "#FF0000"),
            new DefaultMapEntry<String, String>("GREEN", "#00FF00"),
            new DefaultMapEntry<String, String>("BLUE", "#0000FF")
        });
        assertEquals(true, test.containsKey("RED"));
        assertEquals("#FF0000", test.get("RED"));
        assertEquals(true, test.containsKey("GREEN"));
        assertEquals("#00FF00", test.get("GREEN"));
        assertEquals(true, test.containsKey("BLUE"));
        assertEquals("#0000FF", test.get("BLUE"));
        assertEquals(3, test.size());

        
        test = MapUtils.putAll(new HashMap<String, String>(), new Object[] {
            new DefaultKeyValue<String, String>("RED", "#FF0000"),
            new DefaultKeyValue<String, String>("GREEN", "#00FF00"),
            new DefaultKeyValue<String, String>("BLUE", "#0000FF")
        });
        assertEquals(true, test.containsKey("RED"));
        assertEquals("#FF0000", test.get("RED"));
        assertEquals(true, test.containsKey("GREEN"));
        assertEquals("#00FF00", test.get("GREEN"));
        assertEquals(true, test.containsKey("BLUE"));
        assertEquals("#0000FF", test.get("BLUE"));
        assertEquals(3, test.size());
    }

// org.apache.commons.collections4.MapUtilsTest::testConvertResourceBundle
    public void testConvertResourceBundle() {
        final Map<String, String> in = new HashMap<String, String>( 5 , 1 );
        in.put("1", "A");
        in.put("2", "B");
        in.put("3", "C");
        in.put("4", "D");
        in.put("5", "E");

        final ResourceBundle b = new ListResourceBundle() {
            @Override
            public Object[][] getContents() {
                final Object[][] contents = new Object[ in.size() ][2];
                final Iterator<String> i = in.keySet().iterator();
                int n = 0;
                while ( i.hasNext() ) {
                    final Object key = i.next();
                    final Object val = in.get( key );
                    contents[ n ][ 0 ] = key;
                    contents[ n ][ 1 ] = val;
                    ++n;
                }
                return contents;
            }
        };

        final Map<String, Object> out = MapUtils.toMap(b);

        assertTrue( in.equals(out));
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugAndVerbosePrintCasting
    public void testDebugAndVerbosePrintCasting() {
        final Map<Integer, String> inner = new HashMap<Integer, String>(2, 1);
        inner.put(2, "B");
        inner.put(3, "C");

        final Map<Integer, Object> outer = new HashMap<Integer, Object>(2, 1);
        outer.put(0, inner);
        outer.put(1, "A");

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        try {
            MapUtils.debugPrint(outPrint, "Print Map", outer);
        } catch (final ClassCastException e) {
            fail("No Casting should be occurring!");
        }
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugAndVerbosePrintNullMap
    public void testDebugAndVerbosePrintNullMap() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String LABEL = "Print Map";
        outPrint.println(LABEL + " = " + String.valueOf((Object) null));
        final String EXPECTED_OUT = out.toString();

        out.reset();

        MapUtils.debugPrint(outPrint, LABEL, null);
        assertEquals(EXPECTED_OUT, out.toString());

        out.reset();

        MapUtils.verbosePrint(outPrint, LABEL, null);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrintNullLabel
    public void testVerbosePrintNullLabel() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Integer, String> map = new TreeMap<Integer, String>();  
        map.put(2, "B");
        map.put(3, "C");
        map.put(4, null);

        outPrint.println("{");
        outPrint.println(INDENT + "2 = B");
        outPrint.println(INDENT + "3 = C");
        outPrint.println(INDENT + "4 = null");
        outPrint.println("}");
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.verbosePrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrintNullLabel
    public void testDebugPrintNullLabel() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Integer, String> map = new TreeMap<Integer, String>();  
        map.put(2, "B");
        map.put(3, "C");
        map.put(4, null);

        outPrint.println("{");
        outPrint.println(INDENT + "2 = B " + String.class.getName());
        outPrint.println(INDENT + "3 = C " + String.class.getName());
        outPrint.println(INDENT + "4 = null");
        outPrint.println("} " + TreeMap.class.getName());
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.debugPrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrintNullLabelAndMap
    public void testVerbosePrintNullLabelAndMap() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        outPrint.println("null");
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.verbosePrint(outPrint, null, null);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrintNullLabelAndMap
    public void testDebugPrintNullLabelAndMap() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        outPrint.println("null");
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.debugPrint(outPrint, null, null);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrintNullStream
    public void testVerbosePrintNullStream() {
        try {
            MapUtils.verbosePrint(null, "Map", new HashMap<Object, Object>());
            fail("Should generate NullPointerException");
        } catch (final NullPointerException expected) {
        }
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrintNullStream
    public void testDebugPrintNullStream() {
        try {
            MapUtils.debugPrint(null, "Map", new HashMap<Object, Object>());
            fail("Should generate NullPointerException");
        } catch (final NullPointerException expected) {
        }
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrintNullKey
    public void testDebugPrintNullKey() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Object, String> map = new HashMap<Object, String>();
        map.put(null, "A");

        outPrint.println("{");
        outPrint.println(INDENT + "null = A " + String.class.getName());
        outPrint.println("} " + HashMap.class.getName());
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.debugPrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrintNullKey
    public void testVerbosePrintNullKey() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Object, String> map = new HashMap<Object, String>();
        map.put(null, "A");

        outPrint.println("{");
        outPrint.println(INDENT + "null = A");
        outPrint.println("}");
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.verbosePrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrintNullKeyToMap1
    public void testDebugPrintNullKeyToMap1() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Object, Map<?, ?>> map = new HashMap<Object, Map<?, ?>>();
        map.put(null, map);

        outPrint.println("{");
        outPrint.println(INDENT + "null = (this Map) " + HashMap.class.getName());
        outPrint.println("} " + HashMap.class.getName());
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.debugPrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrintNullKeyToMap1
    public void testVerbosePrintNullKeyToMap1() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Object, Map<?, ?>> map = new HashMap<Object, Map<?, ?>>();
        map.put(null, map);

        outPrint.println("{");
        outPrint.println(INDENT + "null = (this Map)");
        outPrint.println("}");
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.verbosePrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrintNullKeyToMap2
    public void testDebugPrintNullKeyToMap2() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Object, Object> map = new HashMap<Object, Object>();
        final Map<Object, Object> map2= new HashMap<Object, Object>();
        map.put(null, map2);
        map2.put("2", "B");

        outPrint.println("{");
        outPrint.println(INDENT + "null = ");
        outPrint.println(INDENT + "{");
        outPrint.println(INDENT + INDENT + "2 = B " + String.class.getName());
        outPrint.println(INDENT + "} " + HashMap.class.getName());
        outPrint.println("} " + HashMap.class.getName());
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.debugPrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrintNullKeyToMap2
    public void testVerbosePrintNullKeyToMap2() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String INDENT = "    ";

        final Map<Object, Object> map = new HashMap<Object, Object>();
        final Map<Object, Object> map2= new HashMap<Object, Object>();
        map.put(null, map2);
        map2.put("2", "B");

        outPrint.println("{");
        outPrint.println(INDENT + "null = ");
        outPrint.println(INDENT + "{");
        outPrint.println(INDENT + INDENT + "2 = B");
        outPrint.println(INDENT + "}");
        outPrint.println("}");
        final String EXPECTED_OUT = out.toString();
        out.reset();

        MapUtils.verbosePrint(outPrint, null, map);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrint
    public void testVerbosePrint() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String LABEL = "Print Map";
        final String INDENT = "    ";

        outPrint.println(LABEL + " = ");
        outPrint.println("{");
        outPrint.println(INDENT + "0 = A");
        outPrint.println(INDENT + "1 = ");
        outPrint.println(INDENT + "{");
        outPrint.println(INDENT + INDENT + "2 = B");
        outPrint.println(INDENT + INDENT + "3 = C");
        outPrint.println(INDENT + "}");
        outPrint.println(INDENT + "7 = (this Map)");
        outPrint.println("}");

        final String EXPECTED_OUT = out.toString();

        out.reset();

        final Map<Integer, String> inner = new TreeMap<Integer, String>();  
        inner.put(2, "B");
        inner.put(3, "C");

        final Map<Integer, Object> outer = new TreeMap<Integer, Object>();
        outer.put(1, inner);
        outer.put(0, "A");
        outer.put(7, outer);

        MapUtils.verbosePrint(outPrint, "Print Map", outer);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrint
    public void testDebugPrint() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String LABEL = "Print Map";
        final String INDENT = "    ";

        outPrint.println(LABEL + " = ");
        outPrint.println("{");
        outPrint.println(INDENT + "0 = A " + String.class.getName());
        outPrint.println(INDENT + "1 = ");
        outPrint.println(INDENT + "{");
        outPrint.println(INDENT + INDENT + "2 = B " + String.class.getName());
        outPrint.println(INDENT + INDENT + "3 = C " + String.class.getName());
        outPrint.println(INDENT + "} " + TreeMap.class.getName());
        outPrint.println(INDENT + "7 = (this Map) " + TreeMap.class.getName());
        outPrint.println("} " + TreeMap.class.getName());

        final String EXPECTED_OUT = out.toString();

        out.reset();

        final Map<Integer, String> inner = new TreeMap<Integer, String>();  
        inner.put(2, "B");
        inner.put(3, "C");

        final Map<Integer, Object> outer = new TreeMap<Integer, Object>();
        outer.put(1, inner);
        outer.put(0, "A");
        outer.put(7, outer);

        MapUtils.debugPrint(outPrint, "Print Map", outer);
        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testVerbosePrintSelfReference
    public void testVerbosePrintSelfReference() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String LABEL = "Print Map";
        final String INDENT = "    ";

        final Map<Integer, Object> grandfather = new TreeMap<Integer, Object>();// treeMap guarantees order across JDKs for test
        final Map<Integer, Object> father = new TreeMap<Integer, Object>();
        final Map<Integer, Object> son    = new TreeMap<Integer, Object>();

        grandfather.put(0, "A");
        grandfather.put(1, father);

        father.put(2, "B");
        father.put(3, grandfather);
        father.put(4, son);

        son.put(5, "C");
        son.put(6, grandfather);
        son.put(7, father);

        outPrint.println(LABEL + " = ");
        outPrint.println("{");
        outPrint.println(INDENT + "0 = A");
        outPrint.println(INDENT + "1 = ");
        outPrint.println(INDENT + "{");
        outPrint.println(INDENT + INDENT + "2 = B");
        outPrint.println(INDENT + INDENT + "3 = (ancestor[0] Map)");
        outPrint.println(INDENT + INDENT + "4 = ");
        outPrint.println(INDENT + INDENT + "{");
        outPrint.println(INDENT + INDENT + INDENT + "5 = C");
        outPrint.println(INDENT + INDENT + INDENT + "6 = (ancestor[1] Map)");
        outPrint.println(INDENT + INDENT + INDENT + "7 = (ancestor[0] Map)");
        outPrint.println(INDENT + INDENT + "}");
        outPrint.println(INDENT + "}");
        outPrint.println("}");

        final String EXPECTED_OUT = out.toString();

        out.reset();
        MapUtils.verbosePrint(outPrint, "Print Map", grandfather);

        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testDebugPrintSelfReference
    public void testDebugPrintSelfReference() {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final PrintStream outPrint = new PrintStream(out);

        final String LABEL = "Print Map";
        final String INDENT = "    ";

        final Map<Integer, Object> grandfather = new TreeMap<Integer, Object>();// treeMap guarantees order across JDKs for test
        final Map<Integer, Object> father = new TreeMap<Integer, Object>();
        final Map<Integer, Object> son    = new TreeMap<Integer, Object>();

        grandfather.put(0, "A");
        grandfather.put(1, father);

        father.put(2, "B");
        father.put(3, grandfather);
        father.put(4, son);

        son.put(5, "C");
        son.put(6, grandfather);
        son.put(7, father);

        outPrint.println(LABEL + " = ");
        outPrint.println("{");
        outPrint.println(INDENT + "0 = A " + String.class.getName());
        outPrint.println(INDENT + "1 = ");
        outPrint.println(INDENT + "{");
        outPrint.println(INDENT + INDENT + "2 = B " + String.class.getName());
        outPrint.println(INDENT + INDENT + "3 = (ancestor[0] Map) " + TreeMap.class.getName());
        outPrint.println(INDENT + INDENT + "4 = ");
        outPrint.println(INDENT + INDENT + "{");
        outPrint.println(INDENT + INDENT + INDENT + "5 = C " + String.class.getName());
        outPrint.println(INDENT + INDENT + INDENT + "6 = (ancestor[1] Map) " + TreeMap.class.getName());
        outPrint.println(INDENT + INDENT + INDENT + "7 = (ancestor[0] Map) " + TreeMap.class.getName());
        outPrint.println(INDENT + INDENT + "} " + TreeMap.class.getName());
        outPrint.println(INDENT + "} " + TreeMap.class.getName());
        outPrint.println("} " + TreeMap.class.getName());

        final String EXPECTED_OUT = out.toString();

        out.reset();
        MapUtils.debugPrint(outPrint, "Print Map", grandfather);

        assertEquals(EXPECTED_OUT, out.toString());
    }

// org.apache.commons.collections4.MapUtilsTest::testEmptyIfNull
    public void testEmptyIfNull() {
        assertTrue(MapUtils.emptyIfNull(null).isEmpty());

        final Map<Long, Long> map = new HashMap<Long, Long>();
        assertSame(map, MapUtils.emptyIfNull(map));
    }

// org.apache.commons.collections4.MapUtilsTest::testIsEmptyWithEmptyMap
    public void testIsEmptyWithEmptyMap() {
        final Map<Object, Object> map = new HashMap<Object, Object>();
        assertEquals(true, MapUtils.isEmpty(map));
    }

// org.apache.commons.collections4.MapUtilsTest::testIsEmptyWithNonEmptyMap
    public void testIsEmptyWithNonEmptyMap() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("item", "value");
        assertEquals(false, MapUtils.isEmpty(map));
    }

// org.apache.commons.collections4.MapUtilsTest::testIsEmptyWithNull
    public void testIsEmptyWithNull() {
        final Map<Object, Object> map = null;
        assertEquals(true, MapUtils.isEmpty(map));
    }

// org.apache.commons.collections4.MapUtilsTest::testIsNotEmptyWithEmptyMap
    public void testIsNotEmptyWithEmptyMap() {
        final Map<Object, Object> map = new HashMap<Object, Object>();
        assertEquals(false, MapUtils.isNotEmpty(map));
    }

// org.apache.commons.collections4.MapUtilsTest::testIsNotEmptyWithNonEmptyMap
    public void testIsNotEmptyWithNonEmptyMap() {
        final Map<String, String> map = new HashMap<String, String>();
        map.put("item", "value");
        assertEquals(true, MapUtils.isNotEmpty(map));
    }

// org.apache.commons.collections4.MapUtilsTest::testIsNotEmptyWithNull
    public void testIsNotEmptyWithNull() {
        final Map<Object, Object> map = null;
        assertEquals(false, MapUtils.isNotEmpty(map));
    }

// org.apache.commons.collections4.MapUtilsTest::testPopulateMap
    public void testPopulateMap() {
        
        final List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("3");
        list.add("5");
        list.add("7");
        list.add("2");
        list.add("4");
        list.add("6");

        
        Map<Object, Object> map = new HashMap<Object, Object>();
        MapUtils.populateMap(map, list, TransformedCollectionTest.STRING_TO_INTEGER_TRANSFORMER);
        assertEquals(list.size(), map.size());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(true, map.containsKey(Integer.valueOf(list.get(i))));
            assertEquals(false, map.containsKey(list.get(i)));
            assertEquals(true, map.containsValue(list.get(i)));
            assertEquals(list.get(i), map.get(Integer.valueOf(list.get(i))));
        }

        
        map = new HashMap<Object, Object>();
        MapUtils.populateMap(map, list, TransformedCollectionTest.STRING_TO_INTEGER_TRANSFORMER, TransformedCollectionTest.STRING_TO_INTEGER_TRANSFORMER);

        assertEquals(list.size(), map.size());
        for (int i = 0; i < list.size(); i++) {
            assertEquals(true, map.containsKey(Integer.valueOf(list.get(i))));
            assertEquals(false, map.containsKey(list.get(i)));
            assertEquals(true, map.containsValue(Integer.valueOf(list.get(i))));
            assertEquals(Integer.valueOf(list.get(i)), map.get(Integer.valueOf(list.get(i))));
        }
    }

// org.apache.commons.collections4.MapUtilsTest::testPopulateMultiMap
    public void testPopulateMultiMap() {
        
        final List<X> list = new ArrayList<X>();
        list.add(new X(1, "x1"));
        list.add(new X(2, "x2"));
        list.add(new X(2, "x3"));
        list.add(new X(5, "x4"));
        list.add(new X(5, "x5"));

        
        final MultiValueMap<Integer, X> map = MultiValueMap.multiValueMap(new TreeMap<Integer, Collection<X>>());
        MapUtils.populateMap(map, list, new Transformer<X, Integer>() {
            public Integer transform(X input) {
                return input.key;
            }
        }, TransformerUtils.<X> nopTransformer());
        assertEquals(list.size(), map.totalSize());

        for (int i = 0; i < list.size(); i++) {
            assertEquals(true, map.containsKey(list.get(i).key));
            assertEquals(true, map.containsValue(list.get(i)));
        }
    }

// org.apache.commons.collections4.MapUtilsTest::testIterableMap
    public void testIterableMap() {
        try {
            MapUtils.iterableMap(null);
            fail("Should throw NullPointerException");
        } catch (final NullPointerException e) {
        }
        final HashMap<String, String> map = new HashMap<String, String>();
        map.put("foo", "foov");
        map.put("bar", "barv");
        map.put("baz", "bazv");
        final IterableMap<String, String> iMap = MapUtils.iterableMap(map);
        assertEquals(map, iMap);
        assertNotSame(map, iMap);
        final HashedMap<String, String> hMap = new HashedMap<String, String>(map);
        assertSame(hMap, MapUtils.iterableMap(hMap));
    }

// org.apache.commons.collections4.MapUtilsTest::testIterableSortedMap
    public void testIterableSortedMap() {
        try {
            MapUtils.iterableSortedMap(null);
            fail("Should throw NullPointerException");
        } catch (final NullPointerException e) {
        }
        final TreeMap<String, String> map = new TreeMap<String, String>();
        map.put("foo", "foov");
        map.put("bar", "barv");
        map.put("baz", "bazv");
        final IterableSortedMap<String, String> iMap = MapUtils.iterableSortedMap(map);
        assertEquals(map, iMap);
        assertNotSame(map, iMap);
        assertSame(iMap, MapUtils.iterableMap(iMap));
    }

// org.apache.commons.collections4.collection.IndexedCollectionTest::testAddedObjectsCanBeRetrievedByKey
    public void testAddedObjectsCanBeRetrievedByKey() throws Exception {
        final Collection<String> coll = makeTestCollection();
        coll.add("12");
        coll.add("16");
        coll.add("1");
        coll.addAll(asList("2","3","4"));

        @SuppressWarnings("unchecked")
        final IndexedCollection<Integer, String> indexed = (IndexedCollection<Integer, String>) coll;
        assertEquals("12", indexed.get(12));
        assertEquals("16", indexed.get(16));
        assertEquals("1", indexed.get(1));
        assertEquals("2", indexed.get(2));
        assertEquals("3", indexed.get(3));
        assertEquals("4", indexed.get(4));
    }

// org.apache.commons.collections4.collection.IndexedCollectionTest::testEnsureDuplicateObjectsCauseException
    public void testEnsureDuplicateObjectsCauseException() throws Exception {
        final Collection<String> coll = makeUniqueTestCollection();

        coll.add("1");
        try {
            coll.add("1");
            fail();
        } catch (final IllegalArgumentException e) {
            
        }
    }

// org.apache.commons.collections4.collection.IndexedCollectionTest::testDecoratedCollectionIsIndexedOnCreation
    public void testDecoratedCollectionIsIndexedOnCreation() throws Exception {
        final Collection<String> original = makeFullCollection();
        final IndexedCollection<Integer, String> indexed = decorateUniqueCollection(original);

        assertEquals("1", indexed.get(1));
        assertEquals("2", indexed.get(2));
        assertEquals("3", indexed.get(3));
    }

// org.apache.commons.collections4.collection.IndexedCollectionTest::testReindexUpdatesIndexWhenDecoratedCollectionIsModifiedSeparately
    public void testReindexUpdatesIndexWhenDecoratedCollectionIsModifiedSeparately() throws Exception {
        final Collection<String> original = new ArrayList<String>();
        final IndexedCollection<Integer, String> indexed = decorateUniqueCollection(original);

        original.add("1");
        original.add("2");
        original.add("3");

        assertNull(indexed.get(1));
        assertNull(indexed.get(2));
        assertNull(indexed.get(3));

        indexed.reindex();

        assertEquals("1", indexed.get(1));
        assertEquals("2", indexed.get(2));
        assertEquals("3", indexed.get(3));
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

// org.apache.commons.collections4.map.MultiValueMapTest::testUnsafeDeSerialization
    public void testUnsafeDeSerialization() throws Exception {
        MultiValueMap map1 = MultiValueMap.multiValueMap(new HashMap(), ArrayList.class);
        byte[] bytes = serialize(map1);
        Object result = deserialize(bytes);
        assertEquals(map1, result);
        
        MultiValueMap map2 = MultiValueMap.multiValueMap(new HashMap(), (Class) String.class);
        bytes = serialize(map2);
        try {
            result = deserialize(bytes);
            fail("unsafe clazz accepted when de-serializing MultiValueMap");
        } catch (UnsupportedOperationException ex) {
            
        }
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
