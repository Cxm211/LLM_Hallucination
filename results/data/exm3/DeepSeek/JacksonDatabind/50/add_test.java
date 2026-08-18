// com/fasterxml/jackson/databind/objectid/ObjectWithCreator1261Test.java
package com.fasterxml.jackson.databind.objectid;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectWithCreator1261Test {
    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
    static class Bean {
        private final int id;
        private Bean ref;

        @JsonCreator
        public Bean(int id) {
            this.id = id;
        }

        public int getId() { return id; }

        public Bean getRef() { return ref; }
        public void setRef(Bean ref) { this.ref = ref; }
    }

    @Test
    public void testForwardReferenceInRegularProperty() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // Create two beans
        Bean bean1 = new Bean(1);
        Bean bean2 = new Bean(2);
        bean1.setRef(bean2); // bean1 references bean2
        // Serialize as array to have forward reference
        String json = mapper.writeValueAsString(new Bean[] { bean1, bean2 });
        // Deserialize
        Bean[] result = mapper.readValue(json, Bean[].class);
        assertNotNull(result);
        assertEquals(2, result.length);
        // The reference should be resolved
        assertSame(result[1], result[0].getRef());
    }
}
