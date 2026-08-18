public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof DelegatingMethod) {
            DelegatingMethod that = (DelegatingMethod) o;
            return this.method.equals(that.method);
        }
        if (o instanceof java.lang.reflect.Method) {
            return this.method.equals(o);
        }
        return false;
    }