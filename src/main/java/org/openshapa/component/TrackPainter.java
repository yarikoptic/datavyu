package org.openshapa.component;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Polygon;
import org.openshapa.component.model.TrackModel;
import org.openshapa.component.model.ViewableModel;

/**
 * This class is used to paint a track and its information
 */
public class TrackPainter extends Component {

    /** Painted region of the carriage*/
    private Polygon carriagePolygon;

    private TrackModel trackModel;

    private ViewableModel viewableModel;

    public TrackPainter() {
        super();
    }

    public TrackModel getTrackModel() {
        return trackModel;
    }

    public void setTrackModel(TrackModel model) {
        this.trackModel = model;
        this.repaint();
    }

    public ViewableModel getViewableModel() {
        return viewableModel;
    }

    public void setViewableModel(ViewableModel viewableModel) {
        this.viewableModel = viewableModel;
        this.repaint();
    }

    public Polygon getCarriagePolygon() {
        return carriagePolygon;
    }  

    @Override
    public void paint(Graphics g) {
        Dimension size = getSize();

        // Paints the background
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, size.width, size.height);


        // If there is an error with track information, don't paint the carriage
        if (trackModel.isErroneous()) {
            g.setColor(Color.red);
            FontMetrics fm = g.getFontMetrics();
            String errorMessage = "Track timing information could not be calculated.";
            int width = fm.stringWidth(errorMessage);
            g.drawString(errorMessage, (size.width / 2) - (width / 2),
                    (size.height / 2) - (fm.getAscent() / 2));
            return;
        }

        // Calculate effective start and end points for the carriage
        float effectiveXOffset;
        /* Calculating carriage width by deleting offsets and remainders because
         * using the displayed scale's measurements will sometimes result in a
         * carriage with a visually inaccurate representation (gap from the
         * displayed end of the carriage to the carriage holder's border)
         */
        float carriageWidth = size.width;

        if (trackModel.getOffset() >= viewableModel.getZoomWindowStart()) {
            /* Absolute value because if the offset is negative we dont want the
             * carriage to grow in size.
             */
            long offset = Math.abs(trackModel.getOffset());
            // Calculate the width taken up by the offset
            effectiveXOffset = ((offset * 1F /
                    viewableModel.getIntervalTime()) *
                    viewableModel.getIntervalWidth());
            // The width of the viewable carriage shrinks
            carriageWidth -= effectiveXOffset;
            /* If offset is negative, the effective offset is always zero
             * because that region of the carriage is never shown.
             */
            if (trackModel.getOffset() < 0) {
                effectiveXOffset = 0;
            }
        } else {
            effectiveXOffset = 0;
        }

        if (trackModel.getDuration() + trackModel.getOffset()
                <= viewableModel.getZoomWindowEnd()) {
            carriageWidth -= (viewableModel.getZoomWindowEnd() -
                    (trackModel.getDuration() + trackModel.getOffset()))
                    / viewableModel.getIntervalTime() *
                    viewableModel.getIntervalWidth();
        }

        int carriageHeight = (int) (size.getHeight() * 8D / 10D);
        int carriageYOffset = (int) (size.getHeight() / 10D);

        // Paint the carriage
        g.setColor(new Color(130, 190, 255)); // Light blue

        // Interactable region
        carriagePolygon = new Polygon();
        carriagePolygon.addPoint(Math.round(effectiveXOffset),
                carriageYOffset);
        carriagePolygon.addPoint(Math.round(effectiveXOffset + carriageWidth - 1),
                carriageYOffset);
        carriagePolygon.addPoint(Math.round(effectiveXOffset + carriageWidth - 1),
                carriageYOffset + carriageHeight);
        carriagePolygon.addPoint(Math.round(effectiveXOffset),
                carriageYOffset + carriageHeight);

        g.fillPolygon(carriagePolygon);

        // Paint the carriage top and bottom outline
        g.setColor(Color.BLUE);
        g.drawLine(Math.round(effectiveXOffset),
                carriageYOffset,
                Math.round(effectiveXOffset + carriageWidth - 1),
                carriageYOffset);
        g.drawLine(Math.round(effectiveXOffset),
                carriageYOffset + carriageHeight,
                Math.round(effectiveXOffset + carriageWidth - 1),
                carriageYOffset + carriageHeight);

        // Determine if the left outline should be painted
        if (trackModel.getOffset() >= viewableModel.getZoomWindowStart()) {
            g.drawLine(Math.round(effectiveXOffset),
                    carriageYOffset,
                    Math.round(effectiveXOffset),
                    carriageYOffset + carriageHeight);
        }

        // Determine if the right outline should be painted
        if (trackModel.getDuration() + trackModel.getOffset()
                <= viewableModel.getZoomWindowEnd()) {
            g.drawLine(Math.round(effectiveXOffset + carriageWidth - 1),
                    carriageYOffset,
                    Math.round(effectiveXOffset + carriageWidth - 1),
                    carriageYOffset + carriageHeight);
        }

    }
}
