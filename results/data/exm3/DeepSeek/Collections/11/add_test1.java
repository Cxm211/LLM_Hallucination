// org/apache/commons/collections/keyvalue/TestMultiKey.java
public void testHashCodeWithNullKeys() {
    Object key = "A";
    MultiKey mk1 = new MultiKey(null, key);
    MultiKey mk2 = new MultiKey(key, null);
    assertTrue("Hash codes should differ when null positions differ", mk1.hashCode() != mk2.hashCode());
}
