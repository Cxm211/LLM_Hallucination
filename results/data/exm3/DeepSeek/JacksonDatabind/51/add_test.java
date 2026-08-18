// com/fasterxml/jackson/databind/jsontype/TestCustomTypeIdResolver.java
public void testPolymorphicTypeViaCustom2() throws Exception {
        Base1270<Poly2> req = new Base1270<Poly2>();
        Poly2 o = new Poly2();
        o.val = "optionValue2";
        req.options = o;
        req.val = "some value2";
        Top1270 top = new Top1270();
        top.b = req;
        String json = MAPPER.writeValueAsString(top);
        JsonNode tree = MAPPER.readTree(json);
        assertNotNull(tree.get("b"));
        assertNotNull(tree.get("b").get("options"));
        assertNotNull(tree.get("b").get("options").get("val"));
        Top1270 itemRead = MAPPER.readValue(json, Top1270.class);
        assertNotNull(itemRead);
        assertNotNull(itemRead.b);
    }
