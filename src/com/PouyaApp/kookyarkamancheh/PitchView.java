

package com.PouyaApp.kookyarkamancheh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class PitchView extends View {
    private float centerPitch, currentPitch;
    private int width, height;
    private final Paint paint = new Paint();

    public PitchView(Context context) {
        super(context);
      }

    public PitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PitchView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setCenterPitch(float centerPitch) {
        this.centerPitch = centerPitch;
        invalidate();
    }

    public void setCurrentPitch(float currentPitch) {
        this.currentPitch = currentPitch;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h/2, oldw, oldh);
        width = w;
        height =h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
    	
    	
        float halfWidth = width / 2;
        paint.setStrokeWidth(6.0f);
        paint.setColor(Color.WHITE);
        canvas.drawLine(halfWidth, 0, halfWidth, height, paint);


        float dx = (currentPitch - centerPitch) / 2;
        if (-1 < dx && dx < 1) {
            paint.setStrokeWidth(4.0f);
            paint.setColor(Color.rgb(85, 107, 47));


            
        } else {
        	paint.setColor(Color.RED);
            paint.setStrokeWidth(8.0f);
            
            if(dx<-1) {
                paint.setTextSize(width/20);
                paint.setColor(Color.YELLOW);
                canvas.drawText("سیم را سفت کنید", 0,height/4 , paint);
            }
            else{
                paint.setTextSize(width/20);
                paint.setColor(Color.YELLOW);
                canvas.drawText("سیم را شل کنید",(width-width/3),height/4 , paint);
            }
            paint.setColor(Color.RED);
            dx = (dx < 0) ? -1 : 1;
        }
        double phi = dx * Math.PI / 4;
        canvas.drawLine(halfWidth, height,
        		halfWidth + (float)Math.sin(phi) * height * 0.9f,
        		height - (float)Math.cos(phi) * height * 0.9f, paint);
    }
}