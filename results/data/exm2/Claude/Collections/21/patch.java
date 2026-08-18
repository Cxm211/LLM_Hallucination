public List<E> subList(final int fromIndex, final int toIndex) {
    final List<E> superSubList = super.subList(fromIndex, toIndex);
    final List<E> delegateSubList = ListUtils.unmodifiableList(superSubList);
    final Set<E> subSet = createSetBasedOnList(set, superSubList);
    return new SetUniqueList<E>(delegateSubList, subSet);
}