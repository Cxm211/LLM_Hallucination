// org/apache/commons/collections/keyvalue/TestMultiKey.java
public void testHashCodeOrderSensitivity() {
    MultiKey mk1 = new MultiKey(1, 2);
    MultiKey mk2 = new MultiKey(2, 1);
    assertTrue("Hash codes should differ for different key order", mk1.hashCode() != mk2.hashCode());
}
