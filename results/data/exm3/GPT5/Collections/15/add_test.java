// org/apache/commons/collections/list/TestSetUniqueList.java
public void testSetReplacesWithExistingElementAndUpdatesSet() {
        List list = new LinkedList();
        SetUniqueList decoratedList = SetUniqueList.decorate(list);
        String a = "A";
        String b = "B";
        String c = "C";

        decoratedList.add(a);
        decoratedList.add(b);
        decoratedList.add(c);
        assertEquals(3, decoratedList.size());

        // Replace index 2 (C) with existing element A (at index 0)
        decoratedList.set(2, a);
        // Should remove the old A at index 0 to keep uniqueness
        assertEquals(2, decoratedList.size());
        assertTrue(decoratedList.contains(a));
        assertTrue(decoratedList.contains(b));
        assertFalse(decoratedList.contains(c));

        // Adding C back should be allowed and increase size
        decoratedList.add(c);
        assertEquals(3, decoratedList.size());

        // Adding A again should not increase size
        decoratedList.add(a);
        assertEquals(3, decoratedList.size());
    }