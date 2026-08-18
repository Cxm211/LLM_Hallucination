public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ArgumentMatcher that = (ArgumentMatcher) o;
    return method != null ? method.equals(that.method) : that.method == null;
}