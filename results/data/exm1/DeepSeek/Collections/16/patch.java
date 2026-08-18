    public List subList(int fromIndex, int toIndex) {
        List sub = super.subList(fromIndex, toIndex);
        return new SetUniqueList(new ArrayList(sub), new HashSet(sub));
    }