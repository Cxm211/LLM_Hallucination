// buggy function
    public static <T> T[] add(T[] array, T element) {
        Class<?> type;
        if (array != null){
            type = array.getClass();
        } else if (element != null) {
            type = element.getClass();
        } else {
            type = Object.class;
        }
        @SuppressWarnings("unchecked") // type must be T
        T[] newArray = (T[]) copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    public static <T> T[] add(T[] array, int index, T element) {
        Class<?> clss = null;
        if (array != null) {
            clss = array.getClass().getComponentType();
        } else if (element != null) {
            clss = element.getClass();
        } else {
            return (T[]) new Object[] { null };
        }
        @SuppressWarnings("unchecked") // the add method creates an array of type clss, which is type T
        final T[] newArray = (T[]) add(array, index, element, clss);
        return newArray;
    }

// trigger testcase
// org/apache/commons/lang3/ArrayUtilsAddTest.java::testLANG571
public void testLANG571(){
        String[] stringArray=null;
        String aString=null;
        try {
            @SuppressWarnings("unused")
            String[] sa = ArrayUtils.add(stringArray, aString);
            fail("Should have caused IllegalArgumentException");
        } catch (IllegalArgumentException iae){
            //expected
        }
        try {
            @SuppressWarnings("unused")
            String[] sa = ArrayUtils.add(stringArray, 0, aString);
            fail("Should have caused IllegalArgumentException");
        } catch (IllegalArgumentException iae){
            //expected
        }
    }
