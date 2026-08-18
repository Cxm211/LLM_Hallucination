// com/fasterxml/jackson/databind/deser/TestArrayDeserialization.java
public void testModifyTypeByAnnotationWithNullContentDeserializer() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        BeanWithList bean = mapper.readValue("{\"list\":[1,2,3]}", BeanWithList.class);
        assertNotNull(bean.list);
        assertEquals(3, bean.list.size());
    }
    static class BeanWithList {
        public List<Integer> list;
    }
