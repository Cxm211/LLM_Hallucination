// org/apache/commons/jxpath/ri/model/MixedModelTest.java::testNullCount
public void testNullCount() {
        assertXPathValue(context, "count($null)", new Double(0));
    }