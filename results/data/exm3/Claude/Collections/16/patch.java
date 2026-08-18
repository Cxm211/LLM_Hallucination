public List subList(int fromIndex, int toIndex) {
    List subList = super.subList(fromIndex, toIndex);
    Set subSet = createSetBasedOnExistingSet(set, subList);
    return new SetUniqueList(subList, subSet);
}

private Set createSetBasedOnExistingSet(Set original, List elements) {
    Set newSet;
    try {
        newSet = (Set) original.getClass().newInstance();
    } catch (Exception e) {
        newSet = new java.util.HashSet();
    }
    newSet.addAll(elements);
    return newSet;
}