public Object set(int index, Object object) {
        int pos = indexOf(object);
        Object removed = super.set(index, object);

        if (pos == -1) {
            // replaced with a new unique object
            set.remove(removed);
            set.add(object);
            return removed;
        }
        if (pos == index) {
            // the object is already in the uniq list at the same position
            return removed;
        }

        super.remove(pos);
        set.remove(removed);  // remove the item deleted by the set

        return removed;  // return the item deleted by the set
    }