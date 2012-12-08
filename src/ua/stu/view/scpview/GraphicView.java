package ua.stu.view.scpview;


import ua.stu.scplib.attribute.GraphicAttribute;
import ua.stu.scplib.attribute.GraphicAttributeBase;
import ua.stu.scplib.data.DataHandler;
import and.awt.BasicStroke;
import and.awt.Color;
import and.awt.Shape;
import and.awt.Stroke;
import and.awt.geom.GeneralPath;
import and.awt.geom.Line2D;
import and.awt.geom.Rectangle2D;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.style.LineHeightSpan.WithDensity;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.widget.Scroller;
import net.pbdavey.awt.AwtView;
import net.pbdavey.awt.Font;
import net.pbdavey.awt.Graphics2D;
import net.pbdavey.awt.RenderingHints;

public class GraphicView extends AwtView {
	// object which holds all required data for drawing
	private DataHandler h = null;
	// scrolling
	private final GestureDetector gestureDetector;	
	private final Scroller scroller;
	//scale(zoom)
	private final ScaleGestureDetector scaleGestureDetector;
	private float scaleFactor=(float)1.0;
	// window size params
	private int W = 800;
	private int H = 600;
	// scroll window params
	private int SW = 800;
	private int SH = 600;
	// inch constant
	private float duim = (float) 25.4;
	// screen size in dpi
	private int sizeScreen = 126;
	//set of graphic attributes
	private GraphicAttributeBase g;
	//the number of tiles to display per column
	private int nTilesPerColumn = 12;
	//the number of tiles to display per row (if 1, then nTilesPerColumn should == numberOfChannels)
	private int nTilesPerRow = 1;
	//how may pixels to use to represent one millivolt
	private float yPixelsInMillivolts;
	//how may pixels to use to represent one millisecond
	private float xPixelsInMilliseconds;
	//how much of the sample data to skip, specified in milliseconds from the start of the samples
	private float timeOffsetInMilliSeconds;
	//offset for graphic
	private float xTitlesOffset = sizeScreen/3;
	// how many pixels per mV use for grid
	private float xPixelsGrid;
	// how many pixels per mS use for grid
	private float yPixelsGrid;
	// color map
	Color backgroundColor = Color.white;
	Color curveColor = Color.blue;
	Color boxColor = Color.black;
	Color gridColor = Color.black;
	Color channelNameColor = Color.black;
	// basic font
	Font font = null;
	// any info?
	private boolean fillBackgroundFirst;
	private boolean invert=false;
	
	/*
	 * Scrolling
	 */
	


	public GraphicView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
				gestureDetector = new GestureDetector(context, new MyGestureListener());
				scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureListener());
				scroller = new Scroller(context);
				
				// init scrollbars
		        setHorizontalScrollBarEnabled(true);
		        setVerticalScrollBarEnabled(true);
		        
		        
		        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
		        initializeScrollbars(a);
		        a.recycle();
	}
	
	public GraphicView(Context context, AttributeSet attribSet) {
		super(context, attribSet);
		// TODO Auto-generated constructor stub
				gestureDetector = new GestureDetector(context, new MyGestureListener());
				scaleGestureDetector = new ScaleGestureDetector(context, new MyScaleGestureListener());
				scroller = new Scroller(context);
				// init scrollbars
		        setHorizontalScrollBarEnabled(true);
		        setVerticalScrollBarEnabled(true);
		        
		        
		        TypedArray a = context.obtainStyledAttributes(R.styleable.View);
		        initializeScrollbars(a);
		        a.recycle();
	}
	@Override
    public boolean onTouchEvent(MotionEvent event)
    {
		// check for tap and cancel fling
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN)
		{
			if (!scroller.isFinished()) scroller.abortAnimation();
		}
		
		// handle pinch zoom gesture
		// don't check return value since it is always true
		scaleGestureDetector.onTouchEvent(event);
		
		// check for scroll gesture
		if (gestureDetector.onTouchEvent(event)) return true;

		// check for pointer release 
		if ((event.getPointerCount() == 1) && ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP))
		{
			int newScrollX = getScrollX();
			if (getScaledWidth() < getWidth()) newScrollX = -(getWidth() - getScaledWidth()) / 2;
			else if (getScrollX() < 0) newScrollX = 0;
			else if (getScrollX() > getScaledWidth() - getWidth()) newScrollX = getScaledWidth() - getWidth();

			int newScrollY = getScrollY();
			if (getScaledHeight() < getHeight()) newScrollY = -(getHeight() - getScaledHeight()) / 2;
			else if (getScrollY() < 0) newScrollY = 0;
			else if (getScrollY() > getScaledHeight() - getHeight()) newScrollY = getScaledHeight() - getHeight();

			if ((newScrollX != getScrollX()) || (newScrollY != getScrollY()))
			{
				scroller.startScroll(getScrollX(), getScrollY(), newScrollX - getScrollX(), newScrollY - getScrollY());
				awakenScrollBars(scroller.getDuration());
			}
		}
		
		return true;
    }
	
	 @Override
	    protected int computeHorizontalScrollRange()
	    {
	        return getSW();
	    }

	    @Override
	    protected int computeVerticalScrollRange()
	    {
	        return getSH();
	    }
	    @Override
	    public void computeScroll()
	    {
			if (scroller.computeScrollOffset())
			{
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = scroller.getCurrX();
				int y = scroller.getCurrY();
				scrollTo(x, y);
				if (oldX != getScrollX() || oldY != getScrollY())
				{
					onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
				}

				postInvalidate();
			}
	    }
		@Override
		protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight)
		{
/*			int scrollX = (getSW() < width ? -(width - getSW()) / 2 : getSW() / 2);
			int scrollY = (getSH() < height ? -(height - getSH()) / 2 : getSH() / 2);
			scrollTo(scrollX, scrollY);*/
		}
		
	    private class MyGestureListener extends SimpleOnGestureListener
	    {
	    	@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
			{
				scrollBy((int)distanceX, (int)distanceY);
				return true;
			}
	        @Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
			{
				int fixedScrollX = 0, fixedScrollY = 0;
				int maxScrollX = getScaledWidth(), maxScrollY = getScaledHeight();
				
				if (getScaledWidth() < getWidth())
				{
					fixedScrollX = -(getWidth() - getScaledWidth()) / 2;
					maxScrollX = fixedScrollX + getScaledWidth();
				}
				
				if (getScaledHeight() < getHeight())
				{
					fixedScrollY = -(getHeight() - getScaledHeight()) / 2;
					maxScrollY = fixedScrollY + getScaledHeight();
				}

				boolean scrollBeyondImage = (fixedScrollX < 0) || (fixedScrollX > maxScrollX) || (fixedScrollY < 0) || (fixedScrollY > maxScrollY);
				if (scrollBeyondImage) return false;
				
				scroller.fling(getScrollX(), getScrollY(), -(int)velocityX, -(int)velocityY, 0,getSW() - getWidth(), 0, getSH() - getHeight());
				awakenScrollBars(scroller.getDuration());
				
				return true;
			}
	        
	    }



		private class MyScaleGestureListener implements OnScaleGestureListener
		{
			public boolean onScale(ScaleGestureDetector detector)
			{
				scaleFactor *= detector.getScaleFactor();
				yPixelsInMillivolts*=detector.getScaleFactor();
				xPixelsInMilliseconds*=detector.getScaleFactor();
				int newScrollX = (int)((getScrollX() + detector.getFocusX()) * detector.getScaleFactor() - detector.getFocusX());
				int newScrollY = (int)((getScrollY() + detector.getFocusY()) * detector.getScaleFactor() - detector.getFocusY());
				scrollTo(newScrollX, newScrollY);
				invalidate();
			
				return true;
			}

			public boolean onScaleBegin(ScaleGestureDetector detector)
			{
				return true;
			}

			public void onScaleEnd(ScaleGestureDetector detector)
			{
			}
		}
	
	/*
	 * Initializing
	 */

	public void init() {
		if (h!=null) {
			g = h.getGraphic();
			setYScaleGrid(5);
			setXScaleGrid((float) (12.5));			
		}
	}

	/**
	 * Drawing
	 */
	
	@Override
	public void paint(Graphics2D g2) {
		
		// check DataHandler
		if (h == null) return;
		

		//perfom default init
		init();
	//	System.out.println("scale in paint");
	//	System.out.println(scaleFactor);
		//setting offsets for labels
		int channelNameXOffset = 10;
		int channelNameYOffset = 0;
		font=new Font("Ubuntu",0,(int) (14*scaleFactor));
		
		// setting color map
		g2.setBackground(backgroundColor);
		g2.setColor(backgroundColor);
		setBackground(backgroundColor);
		
		if (fillBackgroundFirst) {
			g2.fill(new Rectangle2D.Float(0,0,getW(),getH()));
		}
		
		float widthOfTileInPixels = getW()/nTilesPerRow;
		float heightOfTileInPixels = getH()/nTilesPerColumn;
		
		
		float widthOfTileInMilliSeconds = widthOfTileInPixels/xPixelsInMilliseconds;
		float widthOfTileGrid = widthOfTileInPixels/xPixelsGrid;
		float heightOfTileGrid = heightOfTileInPixels/yPixelsGrid;

		// first draw boxes around each tile, with anti-aliasing turned on (only way to get consistent thickness)
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(gridColor);
		g2.draw(new Line2D.Float(0,0,getW(),0));
		float drawingOffsetY = 0;
		for (int row=0;row<nTilesPerColumn;++row) {
			float drawingOffsetX = xTitlesOffset;
			for (int col=0;col<nTilesPerRow;++col) {
				g2.setStroke(new BasicStroke((float) 0.6));	
		
				for (float time=0; time<widthOfTileGrid; time+=200*scaleFactor) {
					float x = drawingOffsetX+time*xPixelsGrid;
					g2.draw(new Line2D.Float(x,drawingOffsetY,x,drawingOffsetY+heightOfTileInPixels));
				}

				g2.setStroke(new BasicStroke((float) 0.6));
				for (float milliVolts=-heightOfTileGrid/2; milliVolts<=heightOfTileGrid/2; milliVolts+=0.5*scaleFactor) {
					float y = drawingOffsetY + heightOfTileInPixels/2 + milliVolts/heightOfTileGrid*heightOfTileInPixels;
					g2.draw(new Line2D.Float(drawingOffsetX,y,drawingOffsetX+widthOfTileInPixels,y));
				}
				drawingOffsetX+=widthOfTileInPixels;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}

		g2.setColor(boxColor);
		g2.setStroke(new BasicStroke((float) 1.0));

		drawingOffsetY = 0;
		int channel=0;
		for (int row=0;row<nTilesPerColumn;++row) {
			float drawingOffsetX = 0;
			for (int col=0;col<nTilesPerRow;++col) {
				// Just drawing each bounding line once doesn't seem to help them sometimes
				// being thicker than others ... is this a stroke width problem (better if anti-aliasing on, but then too slow) ?
				if (row == 0)
					g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY,drawingOffsetX+widthOfTileInPixels+xTitlesOffset,drawingOffsetY));					// top
				if (col == 0)
					g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY,drawingOffsetX,drawingOffsetY+heightOfTileInPixels));					// left		
				
				g2.draw(new Line2D.Float(drawingOffsetX,drawingOffsetY+heightOfTileInPixels,drawingOffsetX+widthOfTileInPixels+xTitlesOffset,drawingOffsetY+heightOfTileInPixels));	// bottom
		
				g2.draw(new Line2D.Float(drawingOffsetX+widthOfTileInPixels+xTitlesOffset,drawingOffsetY,drawingOffsetX+widthOfTileInPixels+xTitlesOffset,drawingOffsetY+heightOfTileInPixels));	// right
				if (g.getChannelNames() != null && channel < g.getDisplaySequence().length && 
						g.getDisplaySequence()[channel] < g.getChannelNames().length) {
					String channelName=g.getChannelNames()[g.getDisplaySequence()[channel]];
					if (channelName != null) {
						g2.setStroke(new BasicStroke());
						g2.setColor(channelNameColor);
						g2.setFont(font);
						g2.drawString(channelName,drawingOffsetX+channelNameXOffset,drawingOffsetY+channelNameYOffset+25*scaleFactor);
					}
				}
				
				drawingOffsetX+=widthOfTileInPixels;
				++channel;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);	// ugly without

		g2.setColor(curveColor);
		//setForeground(curveColor);
		g2.setStroke(new BasicStroke((float) 0.7));
		float interceptY = heightOfTileInPixels/2;
		//System.out.println("SamplingIntervalInMilliSeconds");
		//System.out.println(g.getSamplingIntervalInMilliSeconds());
		//System.out.println("timeOffsetInMilliSeconds");
		//System.out.println(timeOffsetInMilliSeconds);
		float widthOfSampleInPixels=g.getSamplingIntervalInMilliSeconds()*xPixelsInMilliseconds;
		int timeOffsetInSamples = (int)(timeOffsetInMilliSeconds/g.getSamplingIntervalInMilliSeconds());
		int widthOfTileInSamples = (int)(widthOfTileInMilliSeconds/g.getSamplingIntervalInMilliSeconds());
		int usableSamples = g.getNumberOfSamplesPerChannel()-timeOffsetInSamples;
		if (usableSamples <= 0) {
			//usableSamples=0;
			return;
		}
		else if (usableSamples > widthOfTileInSamples) {
			usableSamples=widthOfTileInSamples-1;
		}

	drawingOffsetY = 0;
	 channel = 0;
		GeneralPath thePath = new GeneralPath();
		for (int row=0;row<nTilesPerColumn && channel<g.getNumberOfChannels();++row) {
			float drawingOffsetX = xTitlesOffset;

			for (int col=0;col<nTilesPerRow && channel<g.getNumberOfChannels();++col) {
				float yOffset = drawingOffsetY + interceptY;
				short[] samplesForThisChannel = g.getSamples()[g.getDisplaySequence()[channel]];				
				
				int i = timeOffsetInSamples;
				
				float rescaleY = g.getAmplitudeScalingFactorInMilliVolts()[g.getDisplaySequence()[channel]]*yPixelsInMillivolts;
				float fromXValue = drawingOffsetX;

				
				float fromYValue;
				if (invert)
				 fromYValue = yOffset + samplesForThisChannel[i]*rescaleY;
				else
			  fromYValue = yOffset - samplesForThisChannel[i]*rescaleY;	
				thePath.reset();
				thePath.moveTo(fromXValue,fromYValue);
				++i;
				for (int j=1;j<usableSamples;++j) {
					float toXValue = fromXValue + widthOfSampleInPixels;
					float toYValue;
					if (invert)
					 toYValue = yOffset + samplesForThisChannel[i]*rescaleY;
					else 
					 toYValue = yOffset - samplesForThisChannel[i]*rescaleY;
					
					i++;
					if ((int)fromXValue != (int)toXValue || (int)fromYValue != (int)toYValue) {
						thePath.lineTo(toXValue,toYValue);
					}
					fromXValue=toXValue;
					fromYValue=toYValue;
				}
				g2.draw(thePath);
				drawingOffsetX+=widthOfTileInPixels;
				++channel;
			}
			drawingOffsetY+=heightOfTileInPixels;
		}
		
		return;
	}

	/*
	 * Setters & getters
	 */
	
	public GraphicAttributeBase getG() {
		return g;
	}

	public void setG(GraphicAttributeBase g) {
		this.g = g;
	}

	public int getnTilesPerColumn() {
		return nTilesPerColumn;
	}

	public void setnTilesPerColumn(int nTilesPerColumn) {
		this.nTilesPerColumn = nTilesPerColumn;
	}

	public int getnTilesPerRow() {
		return nTilesPerRow;
	}

	public void setnTilesPerRow(int nTilesPerRow) {
		this.nTilesPerRow = nTilesPerRow;
	}

	public float getyPixelsInMillivolts() {
		return yPixelsInMillivolts;
	}

	public void setyPixelsInMillivolts(int yPixelsInMillivolts) {
		this.yPixelsInMillivolts = yPixelsInMillivolts;
	}

	public float getxPixelsInMilliseconds() {
		return xPixelsInMilliseconds;
	}

	public void setxPixelsInMilliseconds(int xPixelsInMilliseconds) {
		this.xPixelsInMilliseconds = xPixelsInMilliseconds;
	}

	public float getTimeOffsetInMilliSeconds() {
		return timeOffsetInMilliSeconds;
	}

	public void setTimeOffsetInMilliSeconds(float timeOffsetInMilliSeconds) {
		this.timeOffsetInMilliSeconds = timeOffsetInMilliSeconds;
	}

	public boolean isFillBackgroundFirst() {
		return fillBackgroundFirst;
	}

	public void setFillBackgroundFirst(boolean fillBackgroundFirst) {
		this.fillBackgroundFirst = fillBackgroundFirst;
	}
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public void setH(DataHandler h) {
		this.h = h;
	}
	
	public void setSW(int sW) {
		SW = sW;
	}

	public void setSH(int sH) {
		SH =sH;
	}
	public int getSW() {
		return  (int) (SW*scaleFactor);
	}

	public int getSH() {
		return (int) (SH*scaleFactor);
	}
	public int getW() {
		return (int) (W*scaleFactor);
	}

	public void setW(int w) {
		W = w;
	}

	public int getH() {
		return (int) (H*scaleFactor);
	}

	public void setHeight(int h) {
		H = h;
	}
	
	public void setFont(String fontName) {
		this.font = new Font(fontName,0,14);
	}
	
	void setGraphicParameters(GraphicAttribute g) {
		this.g = g;
	}
	
	public void setYScale(float millimetersPerMillivolt) {
		this.yPixelsInMillivolts = (7/millimetersPerMillivolt/(duim/sizeScreen));
	}
		
	public void setXScaleGrid(float millimetersPerSecond) {
		this.xPixelsGrid = millimetersPerSecond/(1000*duim/sizeScreen);	
	}
		
	public void setYScaleGrid(float millimetersPerMillivolt) {
		this.yPixelsGrid = millimetersPerMillivolt/(duim/sizeScreen);
	}

	
	public void setXScale(float millimetersPerSecond) {
		this.xPixelsInMilliseconds = (float)( (millimetersPerSecond*(3.15/5)/(1000*duim/sizeScreen)))* scaleFactor;
		this.W=(int) (millimetersPerSecond*31.3);
		this.SW=(int)millimetersPerSecond*32+50;		
	}
	public void setInvert(boolean invert) {
		this.invert = invert;
	}
	
	private int getScaledWidth()
	{		
		return (int)(W * scaleFactor);
	}
	
	private int getScaledHeight()
	{
		return (int)(H * scaleFactor);
		
	}
	public int getScaleFactor() {
		return (int)(scaleFactor*100);
	}

}