public Object set(int index, Object object) {
    int pos = indexOf(object);
    Object removed = super.set(index, object);

    if (pos == -1) {
        // new object, update set
        set.remove(removed);
        set.add(object);
    } else if (pos != index) {
        // duplicate object elsewhere, remove that duplicate and update set
        super.remove(pos);
        set.remove(removed);
    }
    // if pos == index, no changes needed

    return removed;
}