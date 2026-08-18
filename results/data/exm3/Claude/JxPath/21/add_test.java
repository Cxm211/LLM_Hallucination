// org/apache/commons/jxpath/ri/model/MixedModelTest.java
public void testNullLength() {
    JXPathContext ctx = JXPathContext.newContext(new TestNull());
    Object value = ctx.getValue("nothing");
    assertEquals(null, value);
    
    Pointer pointer = ctx.getPointer("nothing");
    assertEquals(1, pointer.getLength());
    
    pointer = ctx.getPointer("child/nothing");
    assertEquals(1, pointer.getLength());
    
    pointer = ctx.getPointer("array[2]");
    assertEquals(1, pointer.getLength());
}