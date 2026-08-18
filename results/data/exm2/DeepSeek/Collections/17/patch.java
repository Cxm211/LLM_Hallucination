    public EqualPredicate(T object) {
        // do not use the DefaultEquator to keep backwards compatibility
        // the DefaultEquator returns also true if the two object references are equal
        this(object, new Equator<T>() {
            public boolean equate(T o1, T o2) {
                return (o1 == o2) || (o1 != null && o1.equals(o2));
            }
        });
    }