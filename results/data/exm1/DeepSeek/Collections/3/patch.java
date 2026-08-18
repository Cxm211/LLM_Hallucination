public static Collection removeAll(Collection collection, Collection remove) {
    List list = new ArrayList(collection);
    list.removeAll(remove);
    return list;
}