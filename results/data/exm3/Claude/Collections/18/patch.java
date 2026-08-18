public boolean retainAll(Collection<?> coll) {
    if (collection.size() == 0) {
        return false;
    }
    boolean result = collection.retainAll(coll);
    if (result == false) {
        return false;
    }
    if (collection.size() == 0) {
        setOrder.clear();
    } else {
        setOrder.retainAll(collection);
    }
    return result;
}