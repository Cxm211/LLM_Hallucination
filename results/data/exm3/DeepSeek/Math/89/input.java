// buggy function
    public void addValue(Object v) {
            addValue((Comparable<?>) v);            
    }

// trigger testcase
// org/apache/commons/math/stat/FrequencyTest.java::testAddNonComparable
public void testAddNonComparable(){
        try {
            f.addValue(new Object()); // This was previously OK
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
        f.clear();
        f.addValue(1);
        try {
            f.addValue(new Object());
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        }
    }
