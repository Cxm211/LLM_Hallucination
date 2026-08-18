public List<E> subList(final int fromIndex, final int toIndex) {
        final List<E> superSubList = super.subList(fromIndex, toIndex);
        final Set<E> subSet = createSetBasedOnList(new java.util.HashSet<E>(), superSubList);
        return new SetUniqueList<E>(superSubList, subSet);
    }