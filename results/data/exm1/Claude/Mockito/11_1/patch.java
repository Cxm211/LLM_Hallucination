public boolean equals(Object o) {
    if (o == null || o == this) {
        return o == this;
    }
    if (!(o instanceof org.mockito.internal.invocation.InvocationMatcher)) {
        return false;
    }
    return method.equals(((org.mockito.internal.invocation.InvocationMatcher) o).method);
}

public int hashCode() {
    return 1;
}