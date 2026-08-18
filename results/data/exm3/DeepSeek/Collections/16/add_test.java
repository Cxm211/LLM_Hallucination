// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSubListContainsNonEmptyAndFull() {
        // Test with HashSet
        List list = new ArrayList();
        List uniqueList = SetUniqueList.decorate(list);

        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        // Partial sublist
        List sub = uniqueList.subList(1, 3);
        assertTrue(sub.contains("B"));
        assertTrue(sub.contains("C"));
        assertFalse(sub.contains("A"));
        assertFalse(sub.contains("D"));

        // Full sublist
        sub = uniqueList.subList(0, uniqueList.size());
        assertTrue(sub.contains("A"));
        assertTrue(sub.contains("B"));
        assertTrue(sub.contains("C"));
        assertFalse(sub.contains("D"));

        // Test with TreeSet via custom class
        list = new ArrayList();
        uniqueList = new SetUniqueList307(list, new java.util.TreeSet());

        uniqueList.add("A");
        uniqueList.add("B");
        uniqueList.add("C");

        // Partial sublist
        sub = uniqueList.subList(1, 3);
        assertTrue(sub.contains("B"));
        assertTrue(sub.contains("C"));
        assertFalse(sub.contains("A"));
        assertFalse(sub.contains("D"));

        // Full sublist
        sub = uniqueList.subList(0, uniqueList.size());
        assertTrue(sub.contains("A"));
        assertTrue(sub.contains("B"));
        assertTrue(sub.contains("C"));
        assertFalse(sub.contains("D"));
    }
