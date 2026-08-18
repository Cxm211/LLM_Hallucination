// org/apache/commons/lang3/ArrayUtilsAddTest.java
public void testAddSingleElement() {
        // both null -> IllegalArgumentException
        try {
            ArrayUtils.add(null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // array null, element non-null
        String[] result = ArrayUtils.add(null, "a");
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals("a", result[0]);
        assertEquals(String.class, result.getClass().getComponentType());

        // array non-null, element null
        String[] array = new String[] {"a"};
        result = ArrayUtils.add(array, null);
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertNull(result[1]);
        assertEquals(String.class, result.getClass().getComponentType());

        // array non-null, element non-null
        array = new String[] {"a"};
        result = ArrayUtils.add(array, "b");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
        assertEquals(String.class, result.getClass().getComponentType());

        // additional test with different type
        Integer[] intArray = new Integer[] {1};
        Integer[] intResult = ArrayUtils.add(intArray, 2);
        assertEquals(2, intResult.length);
        assertEquals(Integer.valueOf(1), intResult[0]);
        assertEquals(Integer.valueOf(2), intResult[1]);
        assertEquals(Integer.class, intResult.getClass().getComponentType());
    }
