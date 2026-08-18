public boolean retainAll(Collection<?> coll) {
    if (collection.size() == 0) {
        return false;
    }
    if (coll.size() == 0) {
        collection.clear();
        setOrder.clear();
        return true;
    }
    boolean result = false;
    for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
        if (!coll.contains(it.next())) {
            it.remove();
            result = true;
        }
    }
    if (result) {
        collection.retainAll(coll);
    }
    return result;
}