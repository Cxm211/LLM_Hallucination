public Object set(int index, Object object) {
    int pos = indexOf(object);
    Object removed = get(index);
    if (pos == index) {
        return super.set(index, object);
    }
    if (pos != -1) {
        super.remove(pos);
        if (pos < index) {
            index--;
        }
    }
    super.set(index, object);
    set.remove(removed);
    set.add(object);
    return removed;
}