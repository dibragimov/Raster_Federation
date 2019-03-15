/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queryBuilder;

import com.vividsolutions.jts.geom.Envelope;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author zurriyot
 */
public class RasterProcessorQueryBoundRect extends RasterProcessor {

    public RasterProcessorQueryBoundRect(Envelope boundRect) {
        this.envelope = boundRect;
    }
    
    @Override
    public void Process() {
        try {
            //building rasdamanQuery from lower and upper corner of the shape
            rasdamanQuery = "&SERVICE=WCS&VERSION=2.0.1&REQUEST=GetCoverage&" +
            "COVERAGEID=Galicia&SUBSET=E("+envelope.getMinX()+","+envelope.getMaxX()+")&" +
            "SUBSET=N("+envelope.getMinY()+","+envelope.getMaxY()+")&FORMAT=application/gml+xml";
            //Sending a query to rasdaman endpoint and getting coverage xml file
            String position = Helper.httpGet(endpoint+rasdamanQuery);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(position));
            doc = builder.parse(src);
            setBoxValues();
            setNumberOfBoxes();
            setBoxSize();
            setStartingPosition();
        } catch (IOException | ParserConfigurationException | DOMException | SAXException ex) {
            Logger.getLogger(RasterProcessorBoundRect.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    protected void setBoxValues() {
        //Splitting string values of all boxes and parsing into double --- storing them in boxValues
        if (doc.getElementsByTagName("tupleList").item(0)!=null) {
            String[] arTupleList = doc.getElementsByTagName("tupleList").item(0).getTextContent().split(",");
            boxValues = new Double[arTupleList.length];
            for (int i = 0; i < arTupleList.length; i++) {
                boxValues[i]=Double.parseDouble(arTupleList[i]);
            }
        }
    }
    
    @Override
    protected void setStartingPosition() {
        //getting starting position from the document;
        if (doc.getElementsByTagName("pos").item(0)!=null) {
            String startPos = doc.getElementsByTagName("pos").item(0).getTextContent();
            String[] arrayP = startPos.split(" ");
            startingPositionX = Double.parseDouble(arrayP[0]);
            startingPositionY = Double.parseDouble(arrayP[1]);
        }
    }
    
    
}
