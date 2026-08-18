// org/apache/commons/math/linear/EigenDecompositionImplTest.java
public void testFlipIfWarrantedOdd() throws Exception {
        int n = 3;
        int step = 1;
        int pingPong = 0;
        double[] work = new double[12];
        for (int i = 0; i < work.length; i++) {
            work[i] = i + 1;
        }
        EigenDecompositionImpl ed = new EigenDecompositionImpl(new double[]{1}, new double[]{0}, MathUtils.SAFE_MIN);
        java.lang.reflect.Field workField = EigenDecompositionImpl.class.getDeclaredField("work");
        workField.setAccessible(true);
        workField.set(ed, work);
        java.lang.reflect.Field pingPongField = EigenDecompositionImpl.class.getDeclaredField("pingPong");
        pingPongField.setAccessible(true);
        pingPongField.setInt(ed, pingPong);
        java.lang.reflect.Method method = EigenDecompositionImpl.class.getDeclaredMethod("flipIfWarranted", int.class, int.class);
        method.setAccessible(true);
        boolean flipped = (Boolean) method.invoke(ed, n, step);
        assertTrue(flipped);
        double[] expected = {12,11,10,9,5,6,7,8,4,3,2,1};
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], work[i], 1.0e-10);
        }
    }
