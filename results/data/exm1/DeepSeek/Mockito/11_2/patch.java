    public boolean equals(Object o) {
        if (o == null || !(o instanceof Invocation)) {
            return false;
        }
        Invocation other = (Invocation) o;
        return method.equals(other.method);
    }