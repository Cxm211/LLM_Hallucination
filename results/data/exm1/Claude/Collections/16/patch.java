public List subList(int fromIndex, int toIndex) {
    List subList = super.subList(fromIndex, toIndex);
    Set subSet = createSetBasedOnList(subList);
    return new SetUniqueList(subList, subSet);
}

private Set createSetBasedOnList(List list) {
    Set newSet = (Set) (set instanceof java.util.TreeSet ? new java.util.TreeSet(((java.util.TreeSet) set).comparator()) : new java.util.HashSet());
    newSet.addAll(list);
    return newSet;
}