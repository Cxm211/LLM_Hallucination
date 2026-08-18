    public Object set(int index, Object object) {
        int pos = indexOf(object);
        Object removed = super.set(index, object);

        if (pos == -1) {
            // object is not currently in the list
            set.add(object);
            set.remove(removed);
            return removed;
        }
        if (pos == index) {
            // the object is already in the uniq list at the same index
            return removed;
        }

        // object already exists at a different index
        super.remove(pos);
        set.remove(removed);  // remove the item deleted by the set

        return removed;  // return the item deleted by the set
    }