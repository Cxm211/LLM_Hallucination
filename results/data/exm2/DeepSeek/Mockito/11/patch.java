    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DelegatingMethod)) return false;
        DelegatingMethod that = (DelegatingMethod) o;
        if (method == null) {
            return that.method == null;
        } else {
            return method.equals(that.method);
        }
    }