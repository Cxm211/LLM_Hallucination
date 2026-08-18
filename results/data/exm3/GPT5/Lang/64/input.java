// buggy function
    public int compareTo(Object other) {
        return iValue - ((ValuedEnum) other).iValue;
    }

// trigger testcase
// org/apache/commons/lang/enums/ValuedEnumTest.java::testCompareTo_otherEnumType
public void testCompareTo_otherEnumType() {
        try {
            ValuedColorEnum.BLUE.compareTo(ValuedLanguageEnum.ENGLISH);
            fail();
        } catch (ClassCastException ex) {
            // expected
        }
    }
