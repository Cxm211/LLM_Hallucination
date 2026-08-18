    public E set(final int index, final E object) {
        final int pos = indexOf(object);
        final E removed = super.set(index, object);

        if (pos != -1 && pos != index) {
            // the object is already in the uniq list
            // (and it hasn't been swapped with itself)
            super.remove(pos); // remove the duplicate by index
        }
        set.add(object); 
        set.remove(removed);
        return removed; // return the item deleted by the set
    }

// trigger testcase
public void testSetCollections444() {
        final SetUniqueList<Integer> lset = new SetUniqueList<Integer>(new ArrayList<Integer>(), new HashSet<Integer>());

        // Duplicate element
        final Integer obj1 = new Integer(1);
        final Integer obj2 = new Integer(2);

        lset.add(obj1);
        lset.add(obj2);
        lset.set(0, obj1);
        assertEquals(2, lset.size());
        assertSame(obj1, lset.get(0));
        assertSame(obj2, lset.get(1));

        assertTrue(lset.contains(obj1));
        assertTrue(lset.contains(obj2));
    }
