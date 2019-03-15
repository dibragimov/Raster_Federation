/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import queryBuilder.RasterProcessorBoundRect;
import queryBuilder.RasterProcessorQueryBoundRect;
import queryBuilder.SpatialProcessor;

/**
 *
 * @author zurriyot
 */
/**
 * We have lots of Polygons for Galicia. We retrieve one of the Polygons from Strabon endpoint,
 * sending a query. Query gives us shape coordinates and bounding rectangle for that shape.
 * Bounding rectangle of Strabon and Rasdaman differs. Because Rasdaman divides the area from very starting 
 * position of Galicia (box size is 200m x 200m). Usually, the one Rasdaman returns is bigger than 
 * Strabon's one and has more cells.
 * To find Rasdaman bounding rectangle of shape, we use Galicia's lower corner and add 200m both vertically 
 * and horizontally until it reaches the Strabon bounding rectangle.
 * Now, we have almost the same Rasdaman boxes as Rasdaman query retrieves (but we did not ask Rasdaman to retrieve,
 * we made those boxes ourselves). We check whether central points of each box are inside the Strabon polygon or not &
 * filter them.
 * We ask Rasdaman to retrieve each box values of filtered boxes and calculate the average.
 * 
 * NOTE: As the boxes that we have created slightly takes lower positions, they cover some small area of the next box.
 * When we retrieve box value for the cell, it returns two values, including the value of the next cell.
 * Therefore, we took the first value as for the original box.
 */
public class Test3 {
    private double offsetposX;
    private double offsetposY;
    
    private final HashMap sortByColumns = new HashMap();
//    
//    public void checkPoints1() {
//        for (int i = 1; i < 7586; i++) {
//            checkPoints(i);
//        }
//    }
    
    public void checkPoints(int id) {
        //Call createGeomteryFromStrabon() which creates a polygon, 
        //taking coordinate points from strabon endpoint AND calls createRasdamanPoints()
        SpatialProcessor sProcessor = new SpatialProcessor(String.valueOf(id));
        Geometry polygon = sProcessor.getPolygon();
        System.out.println(id+". This is the Polygon (spatial data) coming from Strabon endpoint: ");
        System.out.println(polygon);
        
        //Check if rasdaman point is inside the Strabon Polygon, 
        //if not, then do not add it to our list of Points;
        RasterProcessorBoundRect rProcessor = new RasterProcessorBoundRect(sProcessor.getEnvelope());
        rProcessor.Process();
        offsetposX = rProcessor.getBoxSizeX();
        offsetposY = rProcessor.getBoxSizeY();
        Point[] rasdamanPoints = rProcessor.createCentralPointsForBoxes();
        
        System.out.println("\nThese are created points that need to be checked: ");
        for (int i=0; i<rasdamanPoints.length; i++) {
            System.out.println((i+1)+". "+rasdamanPoints[i]);
        }
        List<Point> pointsInsidePolygon = new ArrayList<>();
        HashMap listToDraw = new HashMap();
        System.out.println("\nThese are the points that are inside the Polygon: ");
        for (int i=0; i<rasdamanPoints.length; i++) {
            if (polygon.covers(rasdamanPoints[i])) {
                System.out.println(i+". "+rasdamanPoints[i]);
                pointsInsidePolygon.add(rasdamanPoints[i]);
                listToDraw.put(i, rasdamanPoints[i]);
            }
        }
        
        System.out.println("\nOverall number of points that are inside the Polygon: "+pointsInsidePolygon.size());
        
        makeColumnsFromPoints(pointsInsidePolygon);
        double average = 0;
        double numOfBoxes = 0;
        for (int i=0; i<sortByColumns.size(); i++) {
                Envelope envelope = (Envelope) sortByColumns.get(i);
                System.out.println(envelope);
                RasterProcessorQueryBoundRect rProcessorQuery = new RasterProcessorQueryBoundRect(envelope);
                rProcessorQuery.Process();
                for (int j = 0; j < rProcessorQuery.getBoxValues().length; j++) {
                Double value = rProcessorQuery.getBoxValues()[j];
                numOfBoxes++;
                System.out.println(value);
                average+=value;
            }
        }
        System.out.println("\nAverage elevation value for the region: "+ (average/numOfBoxes));
        
        System.out.println("\nMap: ");
        ShapeDrawer.draw(rProcessor.getNumberOfBoxesX(), rProcessor.getNumberOfBoxesY(), listToDraw);
    }
    /**
     */
    
    public void makeColumnsFromPoints(List<Point> allPoints) {
        for (int i = 0; i < allPoints.size(); i++) {
            //make columns
            List<Point> oneColumn = new ArrayList<>();
            oneColumn.add(allPoints.get(i));
            for (int j = i; j < allPoints.size()-1; j++) {
                if(allPoints.get(i).getX()!=allPoints.get(i+1).getX()) {
                    break;
                }
                else if (allPoints.get(i).getY()+offsetposY==allPoints.get(i+1).getY()) {
                    oneColumn.add(allPoints.get(i+1));
                    allPoints.set(i, allPoints.get(i+1));
                    allPoints.remove(i+1);
                }
            }
            int counter =  0;
            int columnInitialSize = oneColumn.size();
            for (int l = i; l < allPoints.size(); l++) {
                List<Point> matchedPoints = new ArrayList<>();
                int matchCount = 0;
                
                for (int j = counter; j < oneColumn.size(); j++) {
                    for (int k = i; k < allPoints.size(); k++) {
                        if (Math.ceil(oneColumn.get(j).getY())==Math.ceil(allPoints.get(k).getY())&&
                                oneColumn.get(j).getX()+offsetposX==allPoints.get(k).getX()) {
                            matchCount++;
                            matchedPoints.add(allPoints.get(k));
                        }
                    }
                }
                if (matchCount==oneColumn.size()-counter) {
                    int breakIntoParts = 1;
                    for (int j = i; j < allPoints.size(); j++) {
                        if (matchedPoints.get(0).getY()-offsetposY==allPoints.get(j).getY()) {
                            breakIntoParts++;
                        } else if (matchedPoints.get(matchedPoints.size()-1).getY()+offsetposY==allPoints.get(j).getY()) {
                            breakIntoParts++;
                        }
                    }
                    if (breakIntoParts==1||breakIntoParts==2) {
                        counter+=columnInitialSize;
                        oneColumn.addAll(matchedPoints);
                        allPoints.removeAll(matchedPoints);
                    }
                }
            }
            
            System.out.print(i+1+". ");
            int count = 0;
            for (int j = 0; j < oneColumn.size(); j++) {
                count++;
            }
            System.out.println("Number of boxes in the column: "+count);
            
            
            //create bounding box or envelope for each box
            Point lastBox = oneColumn.get(oneColumn.size()-1);
            Point firstBox = oneColumn.get(0);
            double minX = firstBox.getX()-(offsetposX/2);
            double maxX = lastBox.getX()+(offsetposX/2);
            double minY = firstBox.getY()-(offsetposY/2);
            double maxY = lastBox.getY()+(offsetposY/2);
            Envelope envelope = new Envelope(minX,maxX,minY,maxY);
            //add the box to the hashmap
            sortByColumns.put(i, envelope);
        }
    }   
    public void createEnvelopes() {
        for (int i = 0; i < sortByColumns.size(); i++) {
            List<Point> oneColumn = (List<Point>) sortByColumns.get(i);
            Point lastBox = oneColumn.get(oneColumn.size()-1);
            Point firstBox = oneColumn.get(0);
            double minX = firstBox.getX()+offsetposX;
            double maxX = lastBox.getX()-offsetposX;
            double minY = firstBox.getY()-offsetposY;
            double maxY = lastBox.getY()+offsetposY;
            Envelope envelope = new Envelope(minX,maxX,minY,maxY);
        }
    }
//    public BoundingRectangle createBoundRectangleFromPoint(Point p, double boxSizeX, double boxSizeY) {
//            BoundingRectangle r = new BoundingRectangle();
//            r.setLowerCornerX(p.getX()-(boxSizeX/2));
//            r.setLowerCornerY(p.getY()-Math.abs(boxSizeY/2));
//            r.setUpperCornerX(p.getX()+(boxSizeX/2));
//            r.setUpperCornerY(p.getY()+Math.abs(boxSizeY/2));
//            return r;
//    }
    
   
}
