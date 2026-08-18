public EqualPredicate(T object) {
        this.iValue = object;
        this.equator = new Equator<T>() {
            public boolean equate(T o1, T o2) {
                return o1 == null ? o2 == null : o1.equals(o2);
            }
            public int hash(T o) {
                return o == null ? 0 : o.hashCode();
            }
        };
    }