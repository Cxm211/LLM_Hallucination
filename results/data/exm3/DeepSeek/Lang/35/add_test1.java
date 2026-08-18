// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddAtIndex() {
        // both null -> IllegalArgumentException
        try {
            ArrayUtils.add(null, 0, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // array null, element non-null, index 0
        String[] result = ArrayUtils.add(null, 0, "a");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("a", result[0]);
        assertEquals(String.class, result.getClass().getComponentType());

        // array non-null, element null at index 0
        String[] array = new String[] {"b"};
        result = ArrayUtils.add(array, 0, null);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertNull(result[0]);
        assertEquals("b", result[1]);
        assertEquals(String.class, result.getClass().getComponentType());

        // array non-null, element non-null at index 0
        array = new String[] {"b"};
        result = ArrayUtils.add(array, 0, "a");
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);

        // add at end
        array = new String[] {"a"};
        result = ArrayUtils.add(array, 1, "b");
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);

        // add in middle
        array = new String[] {"a", "c"};
        result = ArrayUtils.add(array, 1, "b");
        assertEquals(3, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
        assertEquals("c", result[2]);

        // index out of bounds negative
        try {
            ArrayUtils.add(new String[] {"a"}, -1, "b");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }

        // index out of bounds greater than length
        try {
            ArrayUtils.add(new String[] {"a"}, 2, "b");
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            // expected
        }
    }
