// org/apache/commons/collections/buffer/TestUnboundedFifoBuffer.java
public void testSerializationEmptyAfterRemovals() throws Exception {
    UnboundedFifoBuffer buffer = new UnboundedFifoBuffer();
    buffer.add("A");
    buffer.add("B");
    buffer.remove();
    buffer.remove();
    buffer = (UnboundedFifoBuffer) serializeDeserialize(buffer);
    buffer.add("Foo");
    assertEquals(1, buffer.size());
}
