public boolean retainAll(Collection<?> coll) {
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