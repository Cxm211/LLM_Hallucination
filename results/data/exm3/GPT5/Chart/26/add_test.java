// org/jfree/chart/plot/junit/CategoryPlotTests.java
public void testDrawWithNullInfoRightAxis() {
        boolean success = false;
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            dataset.addValue(1.0, "S1", "C1");
            JFreeChart chart = ChartFactory.createBarChart(
                "Title",
                "Category",
                "Value",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
            );
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            plot.setRangeAxisLocation(AxisLocation.TOP_OR_RIGHT);
            BufferedImage image = new BufferedImage(200 , 100, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = image.createGraphics();
            chart.draw(g2, new Rectangle2D.Double(0, 0, 200, 100), null, null);
            g2.dispose();
            success = true;
        }
        catch (Exception e) {
            success = false;
        }
        assertTrue(success);
    }