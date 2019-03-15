/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package queryBuilder;

import com.vividsolutions.jts.geom.Envelope;

/**
 *
 * @author zurriyot
 */
public class RasterProcessorBoundRect extends RasterProcessor{
    
    private RasterProcessor rProcessor;
    
    public RasterProcessorBoundRect(Envelope envelope) {
        this.envelope = envelope;
    }
    
    @Override
    public void Process() {
        rProcessor= new RasterProcessor();
        rProcessor.Process();
        this.envelope = rProcessor.convertFromStrabonRectToRasdamanRect(envelope);
        setBoxSize();
        setNumberOfBoxes();
        setStartingPosition();
    }

    @Override
    protected void setBoxSize() {
        this.boxSizeX = rProcessor.boxSizeX;
        this.boxSizeY = rProcessor.boxSizeY;
    }

    @Override
    protected void setNumberOfBoxes() {
        numberOfBoxesX = (int) Math.ceil((envelope.getMaxX()-
                envelope.getMinX())/boxSizeX);
        numberOfBoxesY = (int) Math.ceil((envelope.getMaxY()-
                envelope.getMinY())/Math.abs(boxSizeY));
    }

    @Override
    protected void setStartingPosition() {
        startingPositionX = envelope.getMinX()+(boxSizeX/2);
        startingPositionY = envelope.getMaxY()-Math.abs(boxSizeY/2);
    }
    
    
    

}
