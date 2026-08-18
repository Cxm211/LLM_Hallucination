public Object set(int index, Object object) {
    Object removed = super.set(index, object);
    int pos = indexOf(object);

    if (pos == -1 || pos == index) {
        // the object is already in the uniq list
        // (and it hasn't been swapped with itself)
        return removed;
    }

    super.remove(pos);
    set.add(object);  // add the new object to the set

    return removed;  // return the item deleted by the set
}