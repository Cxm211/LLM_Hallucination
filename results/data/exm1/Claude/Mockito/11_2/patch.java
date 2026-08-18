public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InvocationMatcher that = (InvocationMatcher) o;
    return method.equals(that.method) && matchers.equals(that.matchers);
}

public int hashCode() {
    int result = method.hashCode();
    result = 31 * result + matchers.hashCode();
    return result;
}