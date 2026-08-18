    public boolean retainAll(Collection<?> coll) {
        // Use a temporary set for fast contains checks
        Set<?> retainSet = (coll instanceof Set) ? (Set<?>) coll : new HashSet<Object>(coll);
        boolean changed = false;
        Iterator<E> it = setOrder.iterator();
        while (it.hasNext()) {
            E e = it.next();
            if (!retainSet.contains(e)) {
                it.remove();
                collection.remove(e);
                changed = true;
            }
        }
        return changed;
    }