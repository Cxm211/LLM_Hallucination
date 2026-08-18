// org/apache/commons/math3/linear/RealVectorAbstractTest.java
@Test
public void testEbeDivideZeroByZero() {
    OpenMapRealVector a = new OpenMapRealVector(3);
    OpenMapRealVector b = new OpenMapRealVector(3);
    RealVector r = a.ebeDivide(b);
    for (int i = 0; i < 3; i++) {
        Assert.assertTrue(Double.isNaN(r.getEntry(i)));
    }
}