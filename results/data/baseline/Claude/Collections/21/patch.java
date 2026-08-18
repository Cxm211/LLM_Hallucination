public List<E> subList(final int fromIndex, final int toIndex) {
    return new SetUniqueList<E>(super.subList(fromIndex, toIndex), createSetBasedOnList(set, super.subList(fromIndex, toIndex)));
}