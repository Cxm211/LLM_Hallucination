// org/apache/commons/collections/TestExtendedProperties.java
public void testKeySet5() {
        Map regularMap = new HashMap();
        regularMap.put("x", "val1");
        regularMap.put("y", "val2");
        regularMap.put("z", "val3");

        ExtendedProperties p = new ExtendedProperties();
        p.putAll(regularMap);

        Iterator it = p.getKeys();
        Set keys = new HashSet();
        while (it.hasNext()) {
            keys.add(it.next());
        }
        assertEquals(3, keys.size());
        assertTrue(keys.contains("x"));
        assertTrue(keys.contains("y"));
        assertTrue(keys.contains("z"));
    }