public EqualPredicate(T object) {
        // do not use the DefaultEquator to keep backwards compatibility
        // the DefaultEquator returns also true if the two object references are equal
        this(object, new Equator<T>() {
            public boolean equate(T o1, T o2) {
                if (o1 == null) {
                    return o2 == null;
                }
                return o2 != null && o1.equals(o2);
            }
            public int hash(T o) {
                return (o == null) ? 0 : o.hashCode();
            }
        });
    }