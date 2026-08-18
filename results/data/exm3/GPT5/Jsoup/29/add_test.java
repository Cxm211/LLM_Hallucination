// org/jsoup/nodes/DocumentTest.java::testTitles
Document normaliseTitleSpaces = Jsoup.parse("<title>Hello   there</title>");
assertEquals("Hello there", normaliseTitleSpaces.title());