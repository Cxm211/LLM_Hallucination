// org/apache/commons/jxpath/ri/model/XMLModelTestCase.java
public void testProcessingInstructionTestNonPI() {
    assertXPathPointer(context, "//location[1]/preceding::processing-instruction('target')[1]", "");
}