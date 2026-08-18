    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof DelegatingMethod) {
            return method.equals(((DelegatingMethod) o).method);
        }
        if (o instanceof Method) {
            return method.equals(o);
        }
        return false;
    }