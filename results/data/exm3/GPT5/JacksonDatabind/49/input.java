// buggy function
    public Object generateId(Object forPojo) {
        // 04-Jun-2016, tatu: As per [databind#1255], need to consider possibility of
        //    id being generated for "alwaysAsId", but not being written as POJO; regardless,
        //    need to use existing id if there is one:
            id = generator.generateId(forPojo);
        return id;
    }

// trigger testcase
// com/fasterxml/jackson/databind/objectid/AlwaysAsReferenceFirstTest.java::testIssue1255
public void testIssue1255() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper();
        Foo mo = new Foo();
        mo.bar1 = new Bar();
        mo.bar2 = mo.bar1;

        String json = mapper.writeValueAsString(mo);

        Foo result = mapper.readValue(json, Foo.class);
        assertNotNull(result);
    }
