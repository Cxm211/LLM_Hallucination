// ===== FIXED org.apache.commons.collections.list.SetUniqueList :: subList(int, int) [lines 285-289] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Collections/Collections-16-fixed/src/java/org/apache/commons/collections/list/SetUniqueList.java =====
    public List subList(int fromIndex, int toIndex) {
        List superSubList = super.subList(fromIndex, toIndex);
        Set subSet = createSetBasedOnList(set, superSubList);
        return new SetUniqueList(superSubList, subSet);
    }
