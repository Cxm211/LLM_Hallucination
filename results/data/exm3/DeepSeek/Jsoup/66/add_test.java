// org/jsoup/nodes/ElementTest.java
@Test
    public void testCloneMultipleChildren() {
        String html = "<div><span>One</span><span>Two</span></div>";
        Document doc = Jsoup.parse(html);
        Element originalDiv = doc.select("div").first();
        Element clonedDiv = originalDiv.clone();
        assertEquals(originalDiv.children().size(), clonedDiv.children().size());
        for (int i = 0; i < originalDiv.children().size(); i++) {
            Element originalChild = originalDiv.child(i);
            Element clonedChild = clonedDiv.child(i);
            assertNotSame(originalChild, clonedChild);
            assertEquals(originalDiv, originalChild.parent());
            assertEquals(clonedDiv, clonedChild.parent());
        }
        // Modify a cloned child
        clonedDiv.child(0).text("Modified");
        assertEquals("One", originalDiv.child(0).text());
    }
