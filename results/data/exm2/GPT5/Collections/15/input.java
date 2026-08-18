    public Object set(int index, Object object) {
        int pos = indexOf(object);
        Object removed = super.set(index, object);

        if (pos == -1 || pos == index) {
            // the object is already in the uniq list
            // (and it hasn't been swapped with itself)
            return removed;
        }

        super.remove(pos);
        set.remove(removed);  // remove the item deleted by the set

        return removed;  // return the item deleted by the set
    }

// trigger testcase
public void testCollections304() {
        List list = new LinkedList();
        SetUniqueList decoratedList = SetUniqueList.decorate(list);
        String s1 = "Apple";
        String s2 = "Lemon";
        String s3 = "Orange";
        String s4 = "Strawberry";

        decoratedList.add(s1);
        decoratedList.add(s2);
        decoratedList.add(s3);

        assertEquals(3, decoratedList.size());

        decoratedList.set(1, s4);

        assertEquals(3, decoratedList.size());

        decoratedList.add(1, s4);

        assertEquals(3, decoratedList.size());

        decoratedList.add(1, s2);

        assertEquals(4, decoratedList.size());
    }
