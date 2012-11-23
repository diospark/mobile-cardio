package ua.stu.view.fragments;


import ua.stu.scplib.data.DataHandler;
import ua.stu.view.scpview.GraphicView;
import ua.stu.view.scpview.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class ECGPanelFragment extends Fragment implements OnSeekBarChangeListener {
	
	private DataHandler h;	
	
	/**
	 * Длина экрана
	 */
	private int displayWidth;
	/**
	 * Ширина экрана
	 */
	private int displayHeight;
	/**
	 * Максимальная скорость ЕКГ
	 */
	private static int MAX_SPEED = 100;
	/**
	 * Кореляция усиления для слайдера(потому что отображает только целый шаг)
	 */
	private static int CORRECTION_POWER = 4;
	/**
	 * Максимальное усиление ЕКГ
	 */
	private static int MAX_POWER = 16;
	
	private int speed;
	private int power;
	
	private SeekBar sliderSpeed;
	private SeekBar sliderPower;
	
	private TextView speedValue;
	private TextView powerValue;

	private GraphicView graphicView;
	
	public ECGPanelFragment(DataHandler dh){
		this.h=dh;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
	{
		tempInit();
		
		DisplayMetrics metrics = inflater.getContext().getResources().getDisplayMetrics();
		setDisplayWidth(metrics.widthPixels);
		setDisplayHeight(metrics.heightPixels);
		
		View view = inflater.inflate(R.layout.ecg_panel, null);
	
		speedValue = (TextView)view.findViewById(R.id.speed_value);
		powerValue = (TextView)view.findViewById(R.id.power_value);
		graphicView=(GraphicView)view.findViewById(R.id.ecgpanel);
		graphicView.setH(h);
		
		//temporary
		speedValue.setText(getSpeed()+" mm/c");
		powerValue.setText(getPower()+" mV/cm");
		
		sliderSpeed = (SeekBar)view.findViewById(R.id.speed);
		sliderSpeed.setMax(MAX_SPEED);
		sliderSpeed.setProgress(getSpeed());
		sliderSpeed.incrementProgressBy(1);
		sliderSpeed.setOnSeekBarChangeListener(this);
		
		sliderPower = (SeekBar)view.findViewById(R.id.power);
		sliderPower.setMax(MAX_POWER);
		sliderPower.setProgress(getPower());
		sliderSpeed.incrementProgressBy(1);
		sliderPower.setOnSeekBarChangeListener(this);
		
	
	    return view;
	    
	}

	public void tempInit()
	{
		setSpeed(50);
		setPower(0);
	}
	
	public int getDisplayWidth() {
		return displayWidth;
	}
	
	public void setDisplayWidth(int displayWidth) {
		this.displayWidth = displayWidth;
	}

	public int getDisplayHeight() {
		return displayHeight;
	}

	public void setDisplayHeight(int displayHeight) {
		this.displayHeight = displayHeight;
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		// TODO Auto-generated method stub
		float step = progress;
		if (fromUser)
		{
			switch(seekBar.getId())
			{
			case R.id.speed:
				speedValue.setText(progress+" mm/c");
				break;
			case R.id.power:
				powerValue.setText(step/CORRECTION_POWER+" mV/cm");
				break;
			}
		}
	}

	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getPower() {
		return power;
	}

	public void setPower(int power) {
		this.power = power;
	}

}
