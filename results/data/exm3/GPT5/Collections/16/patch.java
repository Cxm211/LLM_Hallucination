public List subList(int fromIndex, int toIndex) {
        List sub = super.subList(fromIndex, toIndex);
        java.util.Set subSet = new java.util.HashSet(sub);
        return new SetUniqueList(sub, subSet);
    }