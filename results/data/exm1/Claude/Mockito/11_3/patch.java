public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    return method.equals(((Object) o).getClass().getDeclaredFields()[0]);
}