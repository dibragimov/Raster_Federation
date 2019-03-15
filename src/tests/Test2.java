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
import java.util.HashMap;
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
public class Test2 {
    
    public void checkPoints() {
        //Call createGeomteryFromStrabon() which creates a polygon, 
        //taking coordinate points from strabon endpoint AND calls createRasdamanPoints()
        SpatialProcessor sProcessor = new SpatialProcessor("7321");
        Geometry polygon = sProcessor.getPolygon();
        System.out.println("This is the Polygon (spatial data) coming from Strabon endpoint: ");
        System.out.println(polygon);
        
        RasterProcessorBoundRect rProcessor = new RasterProcessorBoundRect(sProcessor.getEnvelope());
        rProcessor.Process();
        Point[] rasdamanPoints = rProcessor.createCentralPointsForBoxes();
        
        //Check if rasdaman point is inside the Strabon Polygon, 
        //if not, then do not add it to our list of Points;
        System.out.println("\nThese are created points that need to be checked: ");
        for (int i=0; i<rasdamanPoints.length; i++) {
            System.out.println((i+1)+". "+rasdamanPoints[i]);
        }
        HashMap listToDraw = new HashMap();
        double average = 0;
        System.out.println("\nThese are the points that are inside the Polygon: ");
        for (int i=0; i<rasdamanPoints.length; i++) {
            if (polygon.covers(rasdamanPoints[i])) {
                System.out.println(i+". "+rasdamanPoints[i]);
                Envelope envelope = createBoundRectangleFromPoint(rasdamanPoints[i], 
                        rProcessor.getBoxSizeX(), rProcessor.getBoxSizeY());
                RasterProcessorQueryBoundRect rProcessorQuery = new RasterProcessorQueryBoundRect(envelope);
                rProcessorQuery.Process();
                Double value = rProcessorQuery.getBoxValues()[0];
                average+=value;
                listToDraw.put(i, rasdamanPoints[i]);
            }
        }
        System.out.println("\nOverall number of points that are inside the Polygon: "+listToDraw.size());
        System.out.println("\nAverage elevation value for the region: "+ (average/listToDraw.size()));
        System.out.println("\nMap: ");
        ShapeDrawer.draw(rProcessor.getNumberOfBoxesX(), rProcessor.getNumberOfBoxesY(), listToDraw);
    }
    
    public Envelope createBoundRectangleFromPoint(Point p, double boxSizeX, double boxSizeY) {
            return new Envelope(p.getX()-(boxSizeX/2), p.getX()+(boxSizeX/2), 
                    p.getY()-Math.abs(boxSizeY/2), p.getY()+Math.abs(boxSizeY/2));
    }
}
