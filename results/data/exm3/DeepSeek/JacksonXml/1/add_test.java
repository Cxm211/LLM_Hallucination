// com/fasterxml/jackson/dataformat/xml/lists/NestedUnwrappedLists180Test.java
public void testEmptyElementInSimpleArray() throws Exception
    {
        @JacksonXmlRootElement(localName = "SimpleArrayBean")
        static class SimpleArrayBean {
            @JacksonXmlElementWrapper(useWrapping=false)
            public List<EmptyObj> items;
        }
        @JacksonXmlRootElement(localName = "EmptyObj")
        static class EmptyObj {
            // no fields
        }
        
        String xml = "<SimpleArrayBean><items/><items/></SimpleArrayBean>";
        SimpleArrayBean result = MAPPER.readValue(xml, SimpleArrayBean.class);
        assertNotNull(result.items);
        assertEquals(2, result.items.size());
        assertNotNull(result.items.get(0));
        assertNotNull(result.items.get(1));
    }
