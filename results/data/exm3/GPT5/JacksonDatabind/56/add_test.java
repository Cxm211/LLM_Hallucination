// com/fasterxml/jackson/databind/deser/TestJdkTypes.java::testLocale
assertEquals(new Locale("FI", "fi", "savo"), MAPPER.readValue(quote("fi-FI-savo"), Locale.class));