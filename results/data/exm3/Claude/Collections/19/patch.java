public E set(final int index, final E object) {
    final E removed = super.set(index, object);
    
    if (!removed.equals(object)) {
        final int pos = indexOf(object);
        
        if (pos != -1 && pos != index) {
            // the object is already in the uniq list
            // (and it hasn't been swapped with itself)
            super.remove(pos); // remove the duplicate by index
        }
        set.add(object);
        set.remove(removed);
    }
    
    return removed; // return the item deleted by the set
}