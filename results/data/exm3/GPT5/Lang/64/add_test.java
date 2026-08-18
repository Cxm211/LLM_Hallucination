// org/apache/commons/lang/enums/ValuedEnumTest.java::testCompareTo_otherEnumType_reverse
public void testCompareTo_otherEnumType_reverse() {
        try {
            ValuedLanguageEnum.ENGLISH.compareTo(ValuedColorEnum.BLUE);
            fail();
        } catch (ClassCastException ex) {
            // expected
        }
    }