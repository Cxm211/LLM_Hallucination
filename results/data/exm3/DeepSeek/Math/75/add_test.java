// org/apache/commons/math/stat/FrequencyTest.java
public void testGetPctObjectAdditional() {
        Frequency f = new Frequency();
        f.addValue(10);
        f.addValue(20);
        f.addValue(20);
        f.addValue(30);
        assertEquals(0.25, f.getPct((Object) Integer.valueOf(10)), tolerance);
        assertEquals(0.5, f.getPct((Object) Integer.valueOf(20)), tolerance);
        assertEquals(0.25, f.getPct((Object) Integer.valueOf(30)), tolerance);
        assertEquals(0.0, f.getPct((Object) Integer.valueOf(15)), tolerance);
        assertEquals(0.0, f.getPct((Object) Integer.valueOf(5)), tolerance);
        assertEquals(0.25, f.getPct((Object) Long.valueOf(10)), tolerance);
        assertEquals(0.5, f.getPct((Object) Long.valueOf(20)), tolerance);
        assertEquals(0.0, f.getPct((Object) "foo"), tolerance);
    }
