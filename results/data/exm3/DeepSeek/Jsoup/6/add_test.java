// org/jsoup/nodes/EntitiesTest.java
@Test
    public void supplementaryAndInvalidNumericEntities() {
        // Supplementary character U+10000 (hex)
        String input1 = "&#x10000;";
        String expected1 = new String(Character.toChars(0x10000));
        assertEquals(expected1, Entities.unescape(input1));
        
        // Supplementary character U+1F600 (grinning face) hex
        String input2 = "&#x1F600;";
        String expected2 = new String(Character.toChars(0x1F600));
        assertEquals(expected2, Entities.unescape(input2));
        
        // Supplementary character in decimal: 65536
        String input3 = "&#65536;";
        String expected3 = new String(Character.toChars(65536));
        assertEquals(expected3, Entities.unescape(input3));
        
        // Invalid numeric entity beyond max Unicode code point (hex)
        String input4 = "&#x110000;";
        // Should remain unchanged
        assertEquals(input4, Entities.unescape(input4));
        
        // Invalid numeric entity beyond max (decimal)
        String input5 = "&#1114112;"; // 0x110000 in decimal
        assertEquals(input5, Entities.unescape(input5));
        
        // Valid BMP numeric entity (non-character) U+FFFF
        String input6 = "&#xFFFF;";
        String expected6 = "\\uFFFF";
        assertEquals(expected6, Entities.unescape(input6));
    }
