// org/apache/commons/lang3/RandomStringUtilsTest.java::testLANG805
public void testLANG805() {
        long seed = System.currentTimeMillis();
        assertEquals("aaa", RandomStringUtils.random(3,0,0,false,false,new char[]{'a'},new Random(seed)));
        // also verify behavior when Random is null and chars array is provided
        assertEquals("bb", RandomStringUtils.random(2,0,0,false,false,new char[]{'b'}, null));
    }