public E set(final int index, final E object) {
        final E removed = super.get(index);
        
        if (set.contains(object) && !object.equals(removed)) {
            throw new IllegalArgumentException("Cannot set object (duplicate already exists)");
        }
        
        super.set(index, object);
        set.add(object);
        set.remove(removed);
        return removed;
    }