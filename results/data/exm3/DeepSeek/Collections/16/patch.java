    public List subList(int fromIndex, int toIndex) {
        List sub = super.subList(fromIndex, toIndex);
        return new SetUniqueList(sub, set) {
            public boolean contains(Object o) {
                return sub.contains(o);
            }
        };
    }