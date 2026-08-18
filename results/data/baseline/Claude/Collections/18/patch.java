public boolean retainAll(Collection<?> coll) {
    if (collection.size() == 0) {
        return false;
    }
    boolean result = false;
    for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
        E element = it.next();
        if (!coll.contains(element)) {
            it.remove();
            collection.remove(element);
            result = true;
        }
    }
    return result;
}