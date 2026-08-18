// com/fasterxml/jackson/dataformat/xml/lists/NestedUnwrappedLists180Test.java
public void testEmptyElementAsProperty() throws Exception
    {
        @JacksonXmlRootElement(localName = "SimplePropertyBean")
        static class SimplePropertyBean {
            public EmptyObj empty;
        }
        @JacksonXmlRootElement(localName = "EmptyObj")
        static class EmptyObj {
            // no fields
        }
        
        String xml = "<SimplePropertyBean><empty/></SimplePropertyBean>";
        SimplePropertyBean result = MAPPER.readValue(xml, SimplePropertyBean.class);
        assertNotNull(result.empty);
    }
