public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    try {
        java.lang.reflect.Field f = getClass().getDeclaredField("method");
        f.setAccessible(true);
        Object otherMethod = f.get(o);
        return method == null ? otherMethod == null : method.equals(otherMethod);
    } catch (Exception e) {
        return false;
    }
}