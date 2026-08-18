public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final ThatClass other = (ThatClass) o;
    return method != null ? method.equals(other.method) : other.method == null;
}

public int hashCode() {
    return method != null ? method.hashCode() : 0;
}