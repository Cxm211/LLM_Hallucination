public List<E> subList(final int fromIndex, final int toIndex) {
    final List<E> superSubList = super.subList(fromIndex, toIndex);
    final Set<E> subSet = createSetBasedOnList(set, superSubList);
    return Collections.unmodifiableList(new SetUniqueList<E>(superSubList, subSet));
}