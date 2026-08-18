public boolean addAll(int index, Collection coll) {
    final int sizeBefore = size();
    int currentIndex = index;
    for (final Iterator it = coll.iterator(); it.hasNext();) {
        add(currentIndex++, it.next());
    }
    return sizeBefore != size();
}