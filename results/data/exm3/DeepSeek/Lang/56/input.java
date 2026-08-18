// buggy function
    public String toString() {
        return "FastDateFormat[" + mPattern + "]";
    }

// trigger testcase
// org/apache/commons/lang/time/FastDateFormatTest.java::testLang303
public void testLang303() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
        format = (FastDateFormat) SerializationUtils.deserialize( SerializationUtils.serialize( format ) );
    }
