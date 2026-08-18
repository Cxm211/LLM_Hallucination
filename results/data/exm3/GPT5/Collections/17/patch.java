public EqualPredicate(T object) {
        // do not use the DefaultEquator to keep backwards compatibility
        // Compare strictly via equals; do not short-circuit on reference equality
        this(object, new Equator<T>() {
            @Override
            public boolean equate(T o1, T o2) {
                return (o1 == null) ? (o2 == null) : o1.equals(o2);
            }

            @Override
            public int hash(T o) {
                return (o == null) ? 0 : o.hashCode();
            }
        });
    }