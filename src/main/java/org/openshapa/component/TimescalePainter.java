package org.openshapa.component;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.SimpleTimeZone;
import javax.swing.JComponent;
import org.openshapa.component.model.TimescaleModel;
import org.openshapa.component.model.ViewableModel;

/**
 * This class is used to paint a timescale for a given range of times.
 */
public class TimescalePainter extends JComponent {

    private TimescaleModel timescaleModel;
    private ViewableModel viewableModel;

    public TimescaleModel getTimescaleModel() {
        return timescaleModel;
    }

    public void setTimescaleModel(TimescaleModel timescaleModel) {
        this.timescaleModel = timescaleModel;
        this.repaint();
    }

    public ViewableModel getViewableModel() {
        return viewableModel;
    }

    public void setViewableModel(ViewableModel viewableModel) {
        this.viewableModel = viewableModel;
        this.repaint();
    }

    // standard date format for clock display.
    private SimpleDateFormat clockFormat;
        
    public TimescalePainter() {
        super();
        clockFormat = new SimpleDateFormat("HH:mm:ss:SSS");
        clockFormat.setTimeZone(new SimpleTimeZone(0, "NO_ZONE"));
    }
    /**
     * This method paints the timing scale.
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Dimension size = getSize();

        // Used for calculating string dimensions and offsets
        FontMetrics fm = g.getFontMetrics();
        final int ascent = fm.getAscent();

        // Interval printing and labelling
        final int paddingLeft = timescaleModel.getPaddingLeft();
        final int paddingRight = timescaleModel.getPaddingRight();
        final float intervalTime = viewableModel.getIntervalTime();
        final float intervalWidth = viewableModel.getIntervalWidth();

        for (float x = 0; x <= timescaleModel.getEffectiveWidth();
                x += timescaleModel.getMajorWidth()) {
            g2d.drawLine(Math.round(x + paddingLeft), 0,
                    Math.round(x + paddingLeft), 25);
            g2d.drawLine(Math.round(x + 1 + paddingLeft), 0,
                    Math.round(x + 1 + paddingLeft), 25);

            // What time does this interval represent
            float time =  viewableModel.getZoomWindowStart() + intervalTime*(x/intervalWidth);
            String strTime = clockFormat.format(time);
            // Don't print if the string will be outside of the panel bounds
            if ((x + paddingLeft + fm.stringWidth(strTime) + 3)
                    < (size.width - paddingRight)) {
                g.drawString(strTime, Math.round(x + 3 + paddingLeft),
                        35 - ascent);
            }
        }

        /* Draw the minor intervals separately because mixing minor and major
         * intervals with floating point precision means some major intervals
         * do not get drawn.
         */
        for (float x = 0; x <= timescaleModel.getEffectiveWidth();
                x += intervalWidth) {
             g2d.drawLine(Math.round(x + paddingLeft), 0,
                    Math.round(x + paddingLeft), 10);
        }

        super.paint(g);

        // Draw the padding
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, paddingLeft, size.height);
//        g.fillRect(size.width - paddingRight, 0, paddingRight, size.height);
    }
    
}
