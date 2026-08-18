// com/fasterxml/jackson/databind/jsontype/WrapperObjectWithObjectIdTest.java
public static class TestAnimalWrapper {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT, visible = true)
        public Object animal;
    }
    
    public static class TestDog {
        public String name;
        public int age;
    }
    
    public static class TestEmpty {
        // no fields
    }
    
    public void testTypeIdVisibleWithObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TestDog dog = new TestDog();
        dog.name = "Fido";
        dog.age = 5;
        TestAnimalWrapper wrapper = new TestAnimalWrapper();
        wrapper.animal = dog;
        String json = mapper.writeValueAsString(wrapper);
        TestAnimalWrapper result = mapper.readValue(json, TestAnimalWrapper.class);
        assertNotNull(result);
        assertNotNull(result.animal);
        assertTrue(result.animal instanceof TestDog);
        TestDog resultDog = (TestDog) result.animal;
        assertEquals("Fido", resultDog.name);
        assertEquals(5, resultDog.age);
    }
    
    public void testTypeIdVisibleWithEmptyObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TestEmpty empty = new TestEmpty();
        TestAnimalWrapper wrapper = new TestAnimalWrapper();
        wrapper.animal = empty;
        String json = mapper.writeValueAsString(wrapper);
        TestAnimalWrapper result = mapper.readValue(json, TestAnimalWrapper.class);
        assertNotNull(result);
        assertNotNull(result.animal);
        assertTrue(result.animal instanceof TestEmpty);
    }
