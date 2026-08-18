public List<E> subList(final int fromIndex, final int toIndex) {
        final List<E> superSubList = super.subList(fromIndex, toIndex);
        final Set<E> subSet = createSetBasedOnList(set, superSubList);
        final SetUniqueList<E> sub = new SetUniqueList<E>(superSubList, subSet);
        return java.util.Collections.unmodifiableList(sub);
    }