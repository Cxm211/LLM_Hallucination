public boolean equals(Object o) {
    if (this == o) {
        return true;
    }
    if (!(o instanceof DelegatingMethod)) {
        return false;
    }
    DelegatingMethod other = (DelegatingMethod) o;
    return this.method.equals(other.method);
}