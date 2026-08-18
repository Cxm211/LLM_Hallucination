// com/fasterxml/jackson/databind/deser/exc/ExceptionPathTest.java::testReferenceToStringWithIndexForInnerClass
public void testReferenceToStringWithIndexForInnerClass() {
    JsonMappingException.Reference ref = new JsonMappingException.Reference(new Outer(), 0);
    assertEquals(getClass().getName()+"$Outer[0]", ref.toString());
}