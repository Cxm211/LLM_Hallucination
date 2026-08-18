public List subList(int fromIndex, int toIndex) {
        List sub = super.subList(fromIndex, toIndex);
        return new SetUniqueList(sub, new HashSet(sub));
    }