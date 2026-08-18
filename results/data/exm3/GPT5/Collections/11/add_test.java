// org/apache/commons/collections/keyvalue/TestMultiKey.java
public void testHashCodeWithNullPositionDifference() {
        Object x = new Object();
        MultiKey mk1 = new MultiKey(x);
        MultiKey mk2 = new MultiKey(null, x);
        assertFalse("Hash codes should differ when a null precedes the same element", mk1.hashCode() == mk2.hashCode());
    }