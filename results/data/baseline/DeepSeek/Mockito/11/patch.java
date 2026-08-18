public boolean equals(Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Invocation other = (Invocation) o;
    return method.equals(other.method);
}