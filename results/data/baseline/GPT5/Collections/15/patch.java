public Object set(int index, Object object) {
        int pos = indexOf(object);
        Object removed = super.set(index, object);

        if (pos != -1 && pos != index) {
            // remove the duplicate occurrence of the object
            super.remove(pos);
        }

        // update backing set for removed element
        if (indexOf(removed) == -1) {
            set.remove(removed);
        }

        // if this is a new element, add it to the set
        if (pos == -1) {
            set.add(object);
        }

        return removed;
    }