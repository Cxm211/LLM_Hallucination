        public T create() {
            try {
                return clazz.newInstance();
            } catch (final Exception ex) {
                throw new FunctorException("Cannot instantiate class: " + clazz, ex);
            }
        }

// trigger testcase
public void testUnsafeDeSerialization() throws Exception {
        MultiValueMap map1 = MultiValueMap.multiValueMap(new HashMap(), ArrayList.class);
        byte[] bytes = serialize(map1);
        Object result = deserialize(bytes);
        assertEquals(map1, result);
        
        MultiValueMap map2 = MultiValueMap.multiValueMap(new HashMap(), (Class) String.class);
        bytes = serialize(map2);
        try {
            result = deserialize(bytes);
            fail("unsafe clazz accepted when de-serializing MultiValueMap");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }
