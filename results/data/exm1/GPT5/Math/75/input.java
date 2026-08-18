// buggy code
    public double getPct(Object v) {
        return getCumPct((Comparable<?>) v);
    }

// relevant test
// org.apache.commons.math.stat.FrequencyTest::testCounts
    public void testCounts() {
        assertEquals("total count",0,f.getSumFreq());
        f.addValue(oneL);
        f.addValue(twoL);
        f.addValue(1);
        f.addValue(oneI);
        assertEquals("one frequency count",3,f.getCount(1));
        assertEquals("two frequency count",1,f.getCount(2));
        assertEquals("three frequency count",0,f.getCount(3));
        assertEquals("total count",4,f.getSumFreq());
        assertEquals("zero cumulative frequency", 0, f.getCumFreq(0));
        assertEquals("one cumulative frequency", 3,  f.getCumFreq(1));
        assertEquals("two cumulative frequency", 4,  f.getCumFreq(2));
        assertEquals("Integer argument cum freq",4, f.getCumFreq(Integer.valueOf(2)));
        assertEquals("five cumulative frequency", 4,  f.getCumFreq(5));
        assertEquals("foo cumulative frequency", 0,  f.getCumFreq("foo"));

        f.clear();
        assertEquals("total count",0,f.getSumFreq());

        
        f.addValue("one");
        f.addValue("One");
        f.addValue("oNe");
        f.addValue("Z");
        assertEquals("one cumulative frequency", 1 ,  f.getCount("one"));
        assertEquals("Z cumulative pct", 0.5,  f.getCumPct("Z"), tolerance);
        assertEquals("z cumulative pct", 1.0,  f.getCumPct("z"), tolerance);
        assertEquals("Ot cumulative pct", 0.25,  f.getCumPct("Ot"), tolerance);
        f.clear();

        f = null;
        Frequency f = new Frequency();
        f.addValue(1);
        f.addValue(Integer.valueOf(1));
        f.addValue(Long.valueOf(1));
        f.addValue(2);
        f.addValue(Integer.valueOf(-1));
        assertEquals("1 count", 3, f.getCount(1));
        assertEquals("1 count", 3, f.getCount(Integer.valueOf(1)));
        assertEquals("0 cum pct", 0.2, f.getCumPct(0), tolerance);
        assertEquals("1 pct", 0.6, f.getPct(Integer.valueOf(1)), tolerance);
        assertEquals("-2 cum pct", 0, f.getCumPct(-2), tolerance);
        assertEquals("10 cum pct", 1, f.getCumPct(10), tolerance);

        f = null;
        f = new Frequency(String.CASE_INSENSITIVE_ORDER);
        f.addValue("one");
        f.addValue("One");
        f.addValue("oNe");
        f.addValue("Z");
        assertEquals("one count", 3 ,  f.getCount("one"));
        assertEquals("Z cumulative pct -- case insensitive", 1 ,  f.getCumPct("Z"), tolerance);
        assertEquals("z cumulative pct -- case insensitive", 1 ,  f.getCumPct("z"), tolerance);

        f = null;
        f = new Frequency();
        assertEquals(0L, f.getCount('a'));
        assertEquals(0L, f.getCumFreq('b'));
        TestUtils.assertEquals(Double.NaN, f.getPct('a'), 0.0);
        TestUtils.assertEquals(Double.NaN, f.getCumPct('b'), 0.0);
        f.addValue('a');
        f.addValue('b');
        f.addValue('c');
        f.addValue('d');
        assertEquals(1L, f.getCount('a'));
        assertEquals(2L, f.getCumFreq('b'));
        assertEquals(0.25, f.getPct('a'), 0.0);
        assertEquals(0.5, f.getCumPct('b'), 0.0);
        assertEquals(1.0, f.getCumPct('e'), 0.0);
    }

// org.apache.commons.math.stat.FrequencyTest::testPcts
    public void testPcts() {
        f.addValue(oneL);
        f.addValue(twoL);
        f.addValue(oneI);
        f.addValue(twoI);
        f.addValue(threeL);
        f.addValue(threeL);
        f.addValue(3);
        f.addValue(threeI);
        assertEquals("one pct",0.25,f.getPct(1),tolerance);
        assertEquals("two pct",0.25,f.getPct(Long.valueOf(2)),tolerance);
        assertEquals("three pct",0.5,f.getPct(threeL),tolerance);
        
        assertEquals("three (Object) pct",0.5,f.getPct((Object) (Integer.valueOf(3))),tolerance);
        assertEquals("five pct",0,f.getPct(5),tolerance);
        assertEquals("foo pct",0,f.getPct("foo"),tolerance);
        assertEquals("one cum pct",0.25,f.getCumPct(1),tolerance);
        assertEquals("two cum pct",0.50,f.getCumPct(Long.valueOf(2)),tolerance);
        assertEquals("Integer argument",0.50,f.getCumPct(Integer.valueOf(2)),tolerance);
        assertEquals("three cum pct",1.0,f.getCumPct(threeL),tolerance);
        assertEquals("five cum pct",1.0,f.getCumPct(5),tolerance);
        assertEquals("zero cum pct",0.0,f.getCumPct(0),tolerance);
        assertEquals("foo cum pct",0,f.getCumPct("foo"),tolerance);
    }

// org.apache.commons.math.stat.FrequencyTest::testAdd
    public void testAdd() {
        char aChar = 'a';
        char bChar = 'b';
        String aString = "a";
        f.addValue(aChar);
        f.addValue(bChar);
        try {
            f.addValue(aString);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        try {
            f.addValue(2);
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
        assertEquals("a pct",0.5,f.getPct(aChar),tolerance);
        assertEquals("b cum pct",1.0,f.getCumPct(bChar),tolerance);
        assertEquals("a string pct",0.0,f.getPct(aString),tolerance);
        assertEquals("a string cum pct",0.0,f.getCumPct(aString),tolerance);

        f = new Frequency();
        f.addValue("One");
        try {
            f.addValue(new Integer("One"));
            fail("Expecting IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            
        }
    }

// org.apache.commons.math.stat.FrequencyTest::testAddNonComparable
    public void testAddNonComparable(){
        try {
            f.addValue(new Object()); 
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        f.clear();
        f.addValue(1);
        try {
            f.addValue(new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }

// org.apache.commons.math.stat.FrequencyTest::testEmptyTable
    public void testEmptyTable() {
        assertEquals("freq sum, empty table", 0, f.getSumFreq());
        assertEquals("count, empty table", 0, f.getCount(0));
        assertEquals("count, empty table",0, f.getCount(Integer.valueOf(0)));
        assertEquals("cum freq, empty table", 0, f.getCumFreq(0));
        assertEquals("cum freq, empty table", 0, f.getCumFreq("x"));
        assertTrue("pct, empty table", Double.isNaN(f.getPct(0)));
        assertTrue("pct, empty table", Double.isNaN(f.getPct(Integer.valueOf(0))));
        assertTrue("cum pct, empty table", Double.isNaN(f.getCumPct(0)));
        assertTrue("cum pct, empty table", Double.isNaN(f.getCumPct(Integer.valueOf(0))));
    }

// org.apache.commons.math.stat.FrequencyTest::testToString
    public void testToString(){
        f.addValue(oneL);
        f.addValue(twoL);
        f.addValue(oneI);
        f.addValue(twoI);

        String s = f.toString();
        
        assertNotNull(s);
        BufferedReader reader = new BufferedReader(new StringReader(s));
        try {
            String line = reader.readLine(); 
            assertNotNull(line);

            line = reader.readLine(); 
            assertNotNull(line);

            line = reader.readLine(); 
            assertNotNull(line);

            line = reader.readLine(); 
            assertNull(line);
        } catch(IOException ex){
            fail(ex.getMessage());
        }
    }

// org.apache.commons.math.stat.FrequencyTest::testIntegerValues
    public void testIntegerValues() {
        Comparable<?> obj1 = null;
        obj1 = Integer.valueOf(1);
        Integer int1 = Integer.valueOf(1);
        f.addValue(obj1);
        f.addValue(int1);
        f.addValue(2);
        f.addValue(Long.valueOf(2));
        assertEquals("Integer 1 count", 2, f.getCount(1));
        assertEquals("Integer 1 count", 2, f.getCount(Integer.valueOf(1)));
        assertEquals("Integer 1 count", 2, f.getCount(Long.valueOf(1)));
        assertEquals("Integer 1 cumPct", 0.5, f.getCumPct(1), tolerance);
        assertEquals("Integer 1 cumPct", 0.5, f.getCumPct(Long.valueOf(1)), tolerance);
        assertEquals("Integer 1 cumPct", 0.5, f.getCumPct(Integer.valueOf(1)), tolerance);
        Iterator<?> it = f.valuesIterator();
        while (it.hasNext()) {
            assertTrue(it.next() instanceof Long);
        }
    }

// org.apache.commons.math.stat.FrequencyTest::testSerial
    public void testSerial() {
        f.addValue(oneL);
        f.addValue(twoL);
        f.addValue(oneI);
        f.addValue(twoI);
        assertEquals(f, TestUtils.serializeAndRecover(f));
    }
