public List subList(int fromIndex, int toIndex) {
    List sub = super.subList(fromIndex, toIndex);
    java.util.Set newSet = null;
    try {
        newSet = (java.util.Set) set.getClass().newInstance();
    } catch (Exception e) {
        newSet = new java.util.HashSet();
    }
    newSet.addAll(sub);
    return new SetUniqueList(sub, newSet);
}