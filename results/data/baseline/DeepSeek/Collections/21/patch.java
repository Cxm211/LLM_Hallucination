public List<E> subList(final int fromIndex, final int toIndex) {
    final List<E> superSubList = super.subList(fromIndex, toIndex);
    return new SetUniqueList<E>(superSubList, set);
}