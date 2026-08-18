public boolean addAll(int index, Collection coll) {
    // gets initial size
    final int sizeBefore = size();

    // adds all elements at the specified index
    int currentIndex = index;
    for (final Iterator it = coll.iterator(); it.hasNext();) {
        Object element = it.next();
        add(currentIndex, element);
        // if element was actually added (not a duplicate), increment the index
        if (size() > sizeBefore + (currentIndex - index)) {
            currentIndex++;
        }
    }

    // compares sizes to detect if collection changed
    return sizeBefore != size();
}