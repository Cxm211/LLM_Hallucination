public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    DelegatingMethod that = (DelegatingMethod) o;
    return method.equals(that.method);
}
