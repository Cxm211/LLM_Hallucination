public static Collection removeAll(Collection collection, Collection remove) {
    if (collection == null) {
        throw new NullPointerException("collection must not be null");
    }
    if (remove == null) {
        throw new NullPointerException("remove must not be null");
    }
    List result = new ArrayList(collection);
    result.removeAll(remove);
    return result;
}