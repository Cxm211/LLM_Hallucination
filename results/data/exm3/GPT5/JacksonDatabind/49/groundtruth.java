// ===== FIXED com.fasterxml.jackson.databind.ser.impl.WritableObjectId :: generateId(Object) [lines 46-54] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/JacksonDatabind/JacksonDatabind-49-fixed/src/main/java/com/fasterxml/jackson/databind/ser/impl/WritableObjectId.java =====
    public Object generateId(Object forPojo) {
        // 04-Jun-2016, tatu: As per [databind#1255], need to consider possibility of
        //    id being generated for "alwaysAsId", but not being written as POJO; regardless,
        //    need to use existing id if there is one:
        if (id == null) {
            id = generator.generateId(forPojo);
        }
        return id;
    }
