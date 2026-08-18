public boolean equals(Object o) {
    if (o == null || o instanceof java.lang.reflect.Method) {
        return method.equals(o);
    }
    
    if (o.getClass() == this.getClass()) {
        DelegatingMethod other = (DelegatingMethod) o;
        return method.equals(other.method);
    }
    
    return false;
}

public int hashCode() {
    return method.hashCode();
}