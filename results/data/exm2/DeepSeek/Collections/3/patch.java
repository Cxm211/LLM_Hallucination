public static Collection removeAll(Collection collection, Collection remove) {
    if (collection == null || remove == null) {
        throw new NullPointerException();
    }
    Collection result = new ArrayList();
    for (Object obj : collection) {
        if (!remove.contains(obj)) {
            result.add(obj);
        }
    }
    return result;
}