        public boolean useForType(JavaType t)
        {
            switch (_appliesFor) {
            case NON_CONCRETE_AND_ARRAYS:
                while (t.isArrayType()) {
                    t = t.getContentType();
                }
                // fall through
            case OBJECT_AND_NON_CONCRETE:
//                return t.isJavaLangObject() || 
                return (t.getRawClass() == Object.class)
                        || (!t.isConcrete()
                                // [databind#88] Should not apply to JSON tree models:
                        || TreeNode.class.isAssignableFrom(t.getRawClass()));

            case NON_FINAL:
                while (t.isArrayType()) {
                    t = t.getContentType();
                }
                // [Issue#88] Should not apply to JSON tree models:
                return !t.isFinal() && !TreeNode.class.isAssignableFrom(t.getRawClass());
            default:
            //case JAVA_LANG_OBJECT:
//                return t.isJavaLangObject();
                return (t.getRawClass() == Object.class);
            }
        }

// trigger testcase
public void testArrayWithDefaultTyping() throws Exception
    {
        ObjectMapper mapper = new ObjectMapper()
            .enableDefaultTyping();

        JsonNode array = mapper.readTree("[ 1, 2 ]");
        assertTrue(array.isArray());
        assertEquals(2, array.size());

        JsonNode obj = mapper.readTree("{ \"a\" : 2 }");
        assertTrue(obj.isObject());
        assertEquals(1, obj.size());
        assertEquals(2, obj.path("a").asInt());
    }
