package ua.stu.view.scpview;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

/**
 * Класс для совмесного управления скролом
 * 
 * @author ivan
 * 
 */
public class GestureListener extends SimpleOnGestureListener {
	private GraphicView graphic = null;
	private DrawChanels chanels = null;
	private float currentdistX = 0;
	private float currentdistY = 0;
	// touch mode
	private int touchMode = this.MODE_BASIC;
	
	// gesture modes:
	// basic
	public static final int MODE_BASIC = 0x01;
	// linear
	public static final int MODE_LINEAR = 0x02;

	/**
	 * 
	 * @param v - graphic
	 * @param v1 - chanels
	 */
	public GestureListener(GraphicView v, DrawChanels v1) {
		this.graphic = v;
		this.chanels = v1;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// check touch mode
		switch (this.touchMode) {
			// basic mode
			case GestureListener.MODE_BASIC:
				/**
				 * обработчик для того чтобы скролл не полз вниз
				 */
				if (currentdistY + distanceY >= 0) {
					currentdistY += distanceY;
					chanels.scrollBy((int) 0, (int) distanceY);
					graphic.scrollBy((int) 0, (int) distanceY);
				} else {
					chanels.scrollBy((int) 0, (int) ((-1) * currentdistY));
					graphic.scrollBy((int) 0, (int) ((-1) * currentdistY));
					currentdistY = 0;
				}
				/**
				 * обработчик для того чтобы скролл не полз вправо для графика
				 */
				if (currentdistX + distanceX >= 0) {
					currentdistX += distanceX;
					graphic.scrollBy((int) distanceX, (int) 0);
					graphic.setTime(distanceX);
				} else {
					graphic.scrollBy((int) ((-1) * currentdistX), (int) 0);
					graphic.setTimeInNull();
					currentdistX = 0;
				}
				break;
			// linear mode
			case GestureListener.MODE_LINEAR:
				//TBD
				break;
		}
		return true;
	}
	
	public void setMode(int mode) {
		this.touchMode = mode;
		// set active flag for views
		if (this.graphic != null) this.graphic.setMode(mode);
	}
	
	public int getMode() {
		return touchMode;
	}
}
