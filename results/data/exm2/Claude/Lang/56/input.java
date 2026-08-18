    public String toString() {
        return "FastDateFormat[" + mPattern + "]";
    }

// trigger testcase
public void testLang303() {
        FastDateFormat format = FastDateFormat.getInstance("yyyy/MM/dd");
        format = (FastDateFormat) SerializationUtils.deserialize( SerializationUtils.serialize( format ) );
    }
