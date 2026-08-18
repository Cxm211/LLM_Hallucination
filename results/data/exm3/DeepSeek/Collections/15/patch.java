    public Object set(int index, Object object) {
        int pos = indexOf(object);
        Object removed = super.set(index, object);

        if (pos == -1) {
            // object is new, need to add to set and remove the old element
            set.add(object);
            set.remove(removed);
        } else if (pos != index) {
            // object already exists at a different position
            super.remove(pos);
            set.remove(removed);
        }
        // else pos == index: do nothing

        return removed;
    }