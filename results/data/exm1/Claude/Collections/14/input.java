// buggy code
    protected Object convertKey(Object key) {
        if (key != null) {
            return key.toString().toLowerCase();
        } else {
            return AbstractHashedMap.NULL;
        }
    }

// relevant test
// org.apache.commons.collections.map.TestCaseInsensitiveMap::testCaseInsensitive
    public void testCaseInsensitive() {
        Map map = new CaseInsensitiveMap();
        map.put("One", "One");
        map.put("Two", "Two");
        assertEquals("One", (String) map.get("one"));
        assertEquals("One", (String) map.get("oNe"));
        map.put("two", "Three");
        assertEquals("Three", (String) map.get("Two"));
    }

// org.apache.commons.collections.map.TestCaseInsensitiveMap::testNullHandling
    public void testNullHandling() {
        Map map = new CaseInsensitiveMap();
        map.put("One", "One");
        map.put("Two", "Two");
        map.put(null, "Three");
        assertEquals("Three", (String) map.get(null));
        map.put(null, "Four");
        assertEquals("Four", (String) map.get(null));
        Set keys = map.keySet();
        assertTrue(keys.contains("one"));
        assertTrue(keys.contains("two"));
        assertTrue(keys.contains(null));
        assertEquals(3, keys.size());
    }

// org.apache.commons.collections.map.TestCaseInsensitiveMap::testPutAll
    public void testPutAll() {
        Map map = new HashMap();
        map.put("One", "One");
        map.put("Two", "Two");
        map.put("one", "Three");
        map.put(null, "Four");
        map.put(new Integer(20), "Five");
        Map caseInsensitiveMap = new CaseInsensitiveMap(map);
        assertEquals(4, caseInsensitiveMap.size()); 
        Set keys = caseInsensitiveMap.keySet();
        assertTrue(keys.contains("one"));
        assertTrue(keys.contains("two"));
        assertTrue(keys.contains(null));
        assertTrue(keys.contains(Integer.toString(20)));
        assertEquals(4, keys.size());
        assertTrue(!caseInsensitiveMap.containsValue("One") 
            || !caseInsensitiveMap.containsValue("Three")); 
        assertEquals("Four", caseInsensitiveMap.get(null));
    }

// org.apache.commons.collections.map.TestCaseInsensitiveMap::testClone
    public void testClone() {
        CaseInsensitiveMap map = new CaseInsensitiveMap(10);
        map.put("1", "1");
        Map cloned = (Map) map.clone();
        assertEquals(map.size(), cloned.size());
        assertSame(map.get("1"), cloned.get("1"));
    }

// org.apache.commons.collections.map.TestCaseInsensitiveMap::testLocaleIndependence
    public void testLocaleIndependence() {
        Locale orig = Locale.getDefault();

        Locale[] locales = { Locale.ENGLISH, new Locale("tr"), Locale.getDefault() };

        String[][] data = { 
            { "i", "I" },
            { "\u03C2", "\u03C3" },
            { "\u03A3", "\u03C2" },
            { "\u03A3", "\u03C3" },
        };

        try {
            for (int i = 0; i < locales.length; i++) {
                Locale.setDefault(locales[i]);
                for (int j = 0; j < data.length; j++) {
                    assertTrue("Test data corrupt: " + j, data[j][0].equalsIgnoreCase(data[j][1]));
                    CaseInsensitiveMap map = new CaseInsensitiveMap();
                    map.put(data[j][0], "value");
                    assertEquals(Locale.getDefault() + ": " + j, "value", map.get(data[j][1]));
                }
            }
        } finally {
            Locale.setDefault(orig);
        }
    }
