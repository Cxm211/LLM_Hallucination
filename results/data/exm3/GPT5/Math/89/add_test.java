// org/apache/commons/math/stat/FrequencyTest.java::testAddNonComparable
public void testAddNull(){
        try {
            f.addValue(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }