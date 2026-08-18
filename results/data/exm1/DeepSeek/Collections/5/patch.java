    public boolean addAll(int index, Collection coll) {
        // gets initial size
        final int sizeBefore = size();

        // adds all elements
        for (final Iterator it = coll.iterator(); it.hasNext();) {
            Object obj = it.next();
            if (!set.contains(obj)) {
                list.add(index, obj);
                set.add(obj);
                index++;
            }
        }

        // compares sizes to detect if collection changed
        return sizeBefore != size();
    }