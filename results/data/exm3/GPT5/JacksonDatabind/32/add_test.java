// com/fasterxml/jackson/databind/deser/TestUntypedDeserialization.java::testNestedUntyped989
pojo = r.readValue("{\"a\":{}}\");
assertTrue(pojo.value instanceof Map);