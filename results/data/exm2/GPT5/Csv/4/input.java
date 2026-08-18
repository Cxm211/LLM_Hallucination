    public Map<String, Integer> getHeaderMap() {
        return new LinkedHashMap<String, Integer>(this.headerMap);
    }

// trigger testcase
@Test
    public void testNoHeaderMap() throws Exception {
        final CSVParser parser = CSVParser.parse("a,b,c\n1,2,3\nx,y,z", CSVFormat.DEFAULT);
        Assert.assertNull(parser.getHeaderMap());
    }
