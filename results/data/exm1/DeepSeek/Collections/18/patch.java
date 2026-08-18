public boolean retainAll(Collection<?> coll) {
    boolean result = false;
    Iterator<E> it = setOrder.iterator();
    while (it.hasNext()) {
        E element = it.next();
        if (!coll.contains(element)) {
            it.remove();
            collection.remove(element);
            result = true;
        }
    }
    return result;
}