public boolean retainAll(Collection<?> coll) {
        if (coll == null) {
            throw new NullPointerException("Collection must not be null");
        }
        if (collection.isEmpty()) {
            return false;
        }

        Collection<?> lookup = (coll instanceof Set) ? coll : new HashSet<Object>(coll);
        boolean modified = false;

        for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
            E e = it.next();
            if (!lookup.contains(e)) {
                it.remove();
                collection.remove(e);
                modified = true;
            }
        }
        return modified;
    }