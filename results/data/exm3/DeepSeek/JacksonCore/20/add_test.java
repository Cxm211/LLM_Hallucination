// com/fasterxml/jackson/core/base64/Base64GenerationTest.java
public void testBinaryAsEmbeddedObjectVarious() throws Exception {
        byte[][] testData = {
            new byte[0],
            new byte[]{65},
            new byte[]{65, 66},
            new byte[]{65, 66, 67}
        };
        String[] expectedBase64 = {"", "QQ==", "QUI=", "QUJD"};
        for (int i = 0; i < testData.length; i++) {
            JsonGenerator g;
            StringWriter sw = new StringWriter();
            g = JSON_F.createGenerator(sw);
            g.writeEmbeddedObject(testData[i]);
            g.close();
            assertEquals(quote(expectedBase64[i]), sw.toString());

            ByteArrayOutputStream bytes = new ByteArrayOutputStream(100);
            g = JSON_F.createGenerator(bytes);
            g.writeEmbeddedObject(testData[i]);
            g.close();
            assertEquals(quote(expectedBase64[i]), bytes.toString("UTF-8"));
        }
    }
