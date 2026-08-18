// ===== FIXED org.apache.commons.lang3.ArrayUtils :: add(T[], T) [lines 3288-3301] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-35-fixed/src/main/java/org/apache/commons/lang3/ArrayUtils.java =====
    public static <T> T[] add(T[] array, T element) {
        Class<?> type;
        if (array != null){
            type = array.getClass();
        } else if (element != null) {
            type = element.getClass();
        } else {
            throw new IllegalArgumentException("Arguments cannot both be null");            
        }
        @SuppressWarnings("unchecked") // type must be T
        T[] newArray = (T[]) copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

// ===== FIXED org.apache.commons.lang3.ArrayUtils :: add(T[], int, T) [lines 3567-3579] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Lang/Lang-35-fixed/src/main/java/org/apache/commons/lang3/ArrayUtils.java =====
    public static <T> T[] add(T[] array, int index, T element) {
        Class<?> clss = null;
        if (array != null) {
            clss = array.getClass().getComponentType();
        } else if (element != null) {
            clss = element.getClass();
        } else {
            throw new IllegalArgumentException("Array and element cannot both be null");            
        }
        @SuppressWarnings("unchecked") // the add method creates an array of type clss, which is type T
        final T[] newArray = (T[]) add(array, index, element, clss);
        return newArray;
    }
