    public int compareTo(Object other) {
        return iValue - ((ValuedEnum) other).iValue;
    }

// trigger testcase
public void testCompareTo_otherEnumType() {
        try {
            ValuedColorEnum.BLUE.compareTo(ValuedLanguageEnum.ENGLISH);
            fail();
        } catch (ClassCastException ex) {
            // expected
        }
    }
