public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof DelegatingMethod) {
            DelegatingMethod that = (DelegatingMethod) o;
            return this.method.equals(that.method);
        }
        return false;
    }