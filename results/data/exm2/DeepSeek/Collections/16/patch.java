    public List subList(int fromIndex, int toIndex) {
        List sub = super.subList(fromIndex, toIndex);
        Set newSet = new HashSet();
        newSet.addAll(sub);
        return new SetUniqueList(sub, newSet);
    }