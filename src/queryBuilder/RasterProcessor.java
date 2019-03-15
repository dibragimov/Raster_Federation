/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queryBuilder;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author zurriyot
 */
public class RasterProcessor {
    
    protected final String endpoint = "http://localhost:8080/rasdaman/ows?";
    protected String rasdamanQuery;
    
    protected Document doc;
    
    protected Envelope envelope;
    protected double boxSizeX, boxSizeY, startingPositionX, startingPositionY;
    protected Double[] boxValues;
    protected int numberOfBoxesX, numberOfBoxesY;

    
    public void Process() {
        try {
            //building rasdamanQuery from lower and upper corner of the shape
            rasdamanQuery = "&SERVICE=WCS&VERSION=2.0.1&REQUEST=GetCoverage&COVERAGEID=Galicia&FORMAT=application/gml+xml";
            
            //Sending a query to rasdaman endpoint and getting coverage xml file
            String position = Helper.httpGet(endpoint+rasdamanQuery);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(position));
            doc = builder.parse(src);
            setBoxSize();
            setStartingPosition();
        } catch (IOException | ParserConfigurationException | DOMException | SAXException ex) {
            Logger.getLogger(RasterProcessorBoundRect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Point[] createCentralPointsForBoxes() {
        //Creating an array of points by adding box sizes to the starting positions
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        Point[] points = new Point[numberOfBoxesX*numberOfBoxesY];
        for (int i = 1; i <= points.length; i++) {
            points[i-1] = geometryFactory.createPoint(new Coordinate(startingPositionX, startingPositionY));
            startingPositionY+=boxSizeY;
            if (i%numberOfBoxesY==0) {
                startingPositionX+=boxSizeX;
                startingPositionY-=boxSizeY*numberOfBoxesY;
            }
        }
        return points;
    }
    
    
    protected Envelope convertFromStrabonRectToRasdamanRect(Envelope strabonRect) {
        double minx=0,miny=0,maxx,maxy, position = startingPositionX;
        boolean done = true;
        while (position<strabonRect.getMaxX()) {
            position += boxSizeX;
            if (position>strabonRect.getMinX()&done) {
//                rasdamanRect.setLowerCornerX(position-offsetposX+0.0000000076);
                minx = (position-boxSizeX);
                done=false;
            }
        }
//        rasdamanRect.setUpperCornerX(position+0.0000000078);
        maxx = (position);
        position = startingPositionY;
        done = true;
        while (position<strabonRect.getMaxY()) {
            position += Math.abs(boxSizeY);
            if (position>strabonRect.getMinY()&&done) {
//                rasdamanRect.setLowerCornerY(position-Math.abs(offsetposY)+0.00000306684);
                miny=(position-Math.abs(boxSizeY));
                done=false;
            }
        }
//        rasdamanRect.setUpperCornerY(position+0.00000306963);
        maxy=(position);
        Envelope rasdamanRect = new Envelope(minx,maxx,miny,maxy);
        return rasdamanRect;
    }
    
    protected void setBoxSize() {
        //getting sizes of each box from the document;
        if (doc.getElementsByTagName("offsetVector").item(0)!=null && doc.getElementsByTagName("offsetVector").item(1)!=null) {
            String offsetVectorX = doc.getElementsByTagName("offsetVector").item(0).getTextContent();
            String offsetVectorY = doc.getElementsByTagName("offsetVector").item(1).getTextContent();
            boxSizeX = Double.parseDouble(offsetVectorX.split(" ")[0]);
            boxSizeY = Double.parseDouble(offsetVectorY.split(" ")[1]);
        }
    }
    
    protected void setStartingPosition() {
        //getting starting position from the document;
        if (doc.getElementsByTagName("lowerCorner").item(0)!=null) {
            String startPos = doc.getElementsByTagName("lowerCorner").item(0).getTextContent();
            String[] arrayP = startPos.split(" ");
            startingPositionX = Double.parseDouble(arrayP[0]);
            startingPositionY = Double.parseDouble(arrayP[1]);
        }
    }
    
    protected void setNumberOfBoxes() {
        //getting the number of boxes of two axes (X and Y) from the document
        if (doc.getElementsByTagName("low").item(0)!=null && doc.getElementsByTagName("high").item(0)!=null) {
            String lowPoints = doc.getElementsByTagName("low").item(0).getTextContent();
            String highPoints = doc.getElementsByTagName("high").item(0).getTextContent();
            numberOfBoxesX = Integer.parseInt(highPoints.split(" ")[0])-
                        Integer.parseInt(lowPoints.split(" ")[0])+1;
            numberOfBoxesY = Integer.parseInt(highPoints.split(" ")[1])-
                        Integer.parseInt(lowPoints.split(" ")[1])+1;
        }
    }

    public Double[] getBoxValues() {
        return boxValues;
    }

    public int getNumberOfBoxesX() {
        return numberOfBoxesX;
    }

    public int getNumberOfBoxesY() {
        return numberOfBoxesY;
    }

    public double getBoxSizeX() {
        return boxSizeX;
    }

    public double getBoxSizeY() {
        return boxSizeY;
    }

    public double getStartingPositionX() {
        return startingPositionX;
    }

    public double getStartingPositionY() {
        return startingPositionY;
    }
    
}
