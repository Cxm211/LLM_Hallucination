// org/apache/commons/collections/set/ListOrderedSetTest.java
public void testRetainAllRemovesOrphanedListElements() throws Exception {
        ListOrderedSet<Integer> set = new ListOrderedSet<Integer>();
        set.add(1);
        set.add(2);
        // Access internal fields via reflection
        java.lang.reflect.Field collectionField = set.getClass().getDeclaredField("collection");
        collectionField.setAccessible(true);
        java.lang.reflect.Field setOrderField = set.getClass().getDeclaredField("setOrder");
        setOrderField.setAccessible(true);
        Collection<Integer> internalSet = (Collection<Integer>) collectionField.get(set);
        List<Integer> internalList = (List<Integer>) setOrderField.get(set);
        // Add an extra element only to the list (simulating inconsistency)
        internalList.add(3);
        // Now call retainAll with a collection that contains all elements of the set (1,2)
        boolean changed = set.retainAll(Arrays.asList(1, 2));
        // changed should be false because the set didn't change
        assertFalse(changed);
        // but the list should no longer contain 3
        assertFalse(internalList.contains(3));
        assertEquals(2, internalList.size());
    }
