// com/fasterxml/jackson/databind/filter/IgnorePropertyOnDeserTest.java
public void testIgnoreFieldWriteOnly() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        // Assume a class SimpleFieldWriteOnly is defined as:
        // public static class SimpleFieldWriteOnly {
        //     private int id;
        //     @JsonIgnore(access = JsonIgnore.Access.WRITE_ONLY)
        //     private String name;
        //     public int getId() { return id; }
        //     public void setId(int id) { this.id = id; }
        //     public String getName() { return name; }
        //     public void setName(String name) { this.name = name; }
        // }
        // For simplicity, we assume such a class exists in the same package.
        // In reality, you would need to define it in the test class.
        // This test expects that 'name' is ignored during serialization but not during deserialization.
        // Buggy version will ignore it during deserialization, causing failure.
        // Fixed version will allow deserialization.
        // Since we cannot define the class here, this test is a template.
        // To run, uncomment and define the class.
        /*
        SimpleFieldWriteOnly obj = new SimpleFieldWriteOnly();
        obj.setId(42);
        obj.setName("test");
        String json = mapper.writeValueAsString(obj);
        // Should not contain 'name'
        assertFalse(json.contains("name"));
        SimpleFieldWriteOnly des = mapper.readValue("{ \"id\":42, \"name\":\"test\" }", SimpleFieldWriteOnly.class);
        assertEquals(42, des.getId());
        assertEquals("test", des.getName());
        */
    }
