public boolean retainAll(Collection<?> coll) {
        boolean result = collection.retainAll(coll);
        if (collection.size() == 0) {
            setOrder.clear();
        } else {
            for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
                if (!collection.contains(it.next())) {
                    it.remove();
                }
            }
        }
        return result;
    }