// org/apache/commons/compress/archivers/sevenz/SevenZFileTest.java
public void testDecodeDictionarySizeWithHighBitInSecondByte() throws Exception {
        byte[] props = new byte[5];
        props[0] = (byte) 0x5D;
        props[1] = 0;
        props[2] = (byte) 0x80;
        props[3] = 0;
        props[4] = 0;
        Coder coder = new Coder();
        coder.properties = props;
        Coders.LZMADecoder decoder = new Coders.LZMADecoder();
        InputStream in = new ByteArrayInputStream(new byte[0]);
        InputStream decoded = decoder.decode(in, coder, null);
        assertNotNull(decoded);
        assertTrue(decoded instanceof LZMAInputStream);
        LZMAInputStream lzmaIn = (LZMAInputStream) decoded;
        java.lang.reflect.Field dictField = LZMAInputStream.class.getDeclaredField("dictionarySize");
        dictField.setAccessible(true);
        int dictSize = dictField.getInt(lzmaIn);
        assertEquals(32768, dictSize);
    }
