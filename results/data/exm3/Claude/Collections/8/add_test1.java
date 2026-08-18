// org/apache/commons/collections/buffer/TestUnboundedFifoBuffer.java
public void testCollections220_SingleElement() throws Exception {
    UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
    buffer.add("Single");
    
    buffer = (UnboundedFifoBuffer) serializeDeserialize(buffer);
    
    assertEquals(1, buffer.size());
    assertEquals("Single", buffer.remove());
    
    buffer.add("New1");
    buffer.add("New2");
    assertEquals(2, buffer.size());
}