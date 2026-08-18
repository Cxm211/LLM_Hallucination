// org/apache/commons/collections/buffer/TestUnboundedFifoBuffer.java
public void testCollections220_MultipleElements() throws Exception {
    UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
    buffer.add("A");
    buffer.add("B");
    buffer.add("C");
    
    buffer = (UnboundedFifoBuffer) serializeDeserialize(buffer);
    
    assertEquals(3, buffer.size());
    assertEquals("A", buffer.remove());
    assertEquals("B", buffer.remove());
    assertEquals("C", buffer.remove());
    
    buffer.add("D");
    assertEquals(1, buffer.size());
    assertEquals("D", buffer.remove());
}
