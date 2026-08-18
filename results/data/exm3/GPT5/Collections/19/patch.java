public E set(final int index, final E object) {
        final int pos = indexOf(object);
        final E removed = super.set(index, object);

        if (pos != -1 && pos != index) {
            // the object is already in the uniq list
            // (and it hasn't been swapped with itself)
            super.remove(pos); // remove the duplicate by index
        }
        // First remove the old element from the set, then add the new one to avoid
        // accidentally removing the newly added element when they are equal.
        set.remove(removed);
        set.add(object);
        return removed; // return the item deleted by the set
    }