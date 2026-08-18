    public boolean retainAll(Collection<?> coll) {
        boolean result = collection.retainAll(coll);
        if (collection.size() == 0) {
            if (!setOrder.isEmpty()) {
                setOrder.clear();
                result = true;
            }
        } else {
            for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
                E next = it.next();
                if (!collection.contains(next)) {
                    it.remove();
                    result = true;
                }
            }
        }
        return result;
    }