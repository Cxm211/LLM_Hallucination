    public boolean equals(Object o) {
        if (!(o instanceof InvocationImpl)) {
            return false;
        }
        InvocationImpl other = (InvocationImpl) o;
        return method == null ? other.method == null : method.equals(other.method);
    }