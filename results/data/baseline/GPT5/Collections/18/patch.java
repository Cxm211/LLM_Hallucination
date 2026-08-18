public boolean retainAll(Collection<?> coll) {
        boolean result = collection.retainAll(coll);
        if (collection.isEmpty()) {
            setOrder.clear();
        } else {
            for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
                E e = it.next();
                if (!collection.contains(e)) {
                    it.remove();
                }
            }
        }
        return result;
    }