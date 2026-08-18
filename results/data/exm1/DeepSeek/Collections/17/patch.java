    public EqualPredicate(T object) {
        this(object, new Equator<T>() {
            public boolean equate(T o1, T o2) {
                return (o1 == null ? o2 == null : o1.equals(o2));
            }
        });
    }