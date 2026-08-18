public boolean retainAll(Collection<?> coll) {
        // Optimize retainAll to avoid quadratic behavior when coll has slow contains()
        Collection<?> lookup = (coll instanceof Set<?>) ? coll : new HashSet<Object>(coll);
        boolean modified = false;
        for (Iterator<E> it = collection.iterator(); it.hasNext();) {
            E e = it.next();
            if (!lookup.contains(e)) {
                it.remove();
                modified = true;
            }
        }
        if (!modified) {
            return false;
        }
        if (collection.size() == 0) {
            setOrder.clear();
        } else {
            for (Iterator<E> it = setOrder.iterator(); it.hasNext();) {
                if (!collection.contains(it.next())) {
                    it.remove();
                }
            }
        }
        return true;
    }