package com.epiclancers.customview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.List;
import java.util.logging.Handler;

public class ModulesView extends View {

    private boolean[] modules;
    private float shapeWidth;
    private float shapeSpacing;
    private float outlineWidth;
    private Rect[] rectangles;
    private Paint paintMain;
    private Paint paintBlack;
    private float radius;
    private int mMaxHorizontalModules;
    private int outlineColor;
    private int shapeValue;
    private ModuleStatusAccessibilityHelper moduleStatusAccessibilityHelper;

    public boolean[] getModules() {
        return modules;
    }

    public void setModules(boolean[] modules) {
        this.modules = modules;
    }

    public ModulesView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModulesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModulesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        moduleStatusAccessibilityHelper = new ModuleStatusAccessibilityHelper(this);
        ViewCompat.setAccessibilityDelegate( this, moduleStatusAccessibilityHelper );


        TypedArray a = getContext().obtainStyledAttributes(attrs,R.styleable.ModulesView,defStyle,0);

        outlineColor = a.getColor( R.styleable.ModulesView_outlineColor , Color.BLACK );
        shapeValue = a.getInt( R.styleable.ModulesView_shape , 0 );
        outlineWidth = a.getDimension( R.styleable.ModulesView_outlineWidth , 6f );

        a.recycle();

        if (isInEditMode())
            setUpDemoModules();

        shapeWidth = 70f;
        shapeSpacing = 10f;

        paintBlack = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintBlack.setColor(outlineColor);
        paintBlack.setStyle(Paint.Style.STROKE);
        paintBlack.setStrokeWidth(outlineWidth);

        int colorMain = getContext().getResources().getColor(R.color.colorAccent);
        paintMain = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintMain.setColor(colorMain);
        paintMain.setStyle(Paint.Style.FILL);

        radius = (int) (shapeWidth-shapeSpacing)/2;
    }


    private void setUpDemoModules() {
        boolean[] modules = new boolean[34];
        for (int i = 0; i < modules.length/2; i++) {
            modules[i] = true;
        }
        setModules(modules);
    }

    private void setUpModuleRectangles(int width) {
        int availableWidth = width - getPaddingLeft() - getPaddingRight();
        int numberOfModulesThatCanFit = (int)(availableWidth / (shapeWidth+shapeSpacing));
        int mMaxHorizontalModules = Math.min( numberOfModulesThatCanFit , modules.length );

        rectangles = new Rect[modules.length];
        for (int i = 0; i < rectangles.length; i++) {
            int column = i % mMaxHorizontalModules;
            int row = i / mMaxHorizontalModules;
            int x = getPaddingLeft() + (int)  ( column * (shapeSpacing+shapeWidth));
            int y = getPaddingTop() + (int) (row * (shapeSpacing+shapeWidth));
            rectangles[i] = new Rect( x , y, x + (int) shapeWidth , y + (int) shapeWidth );
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setUpModuleRectangles(w);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                int moduleClicked = findModuleSelected(event.getX() , event.getY());
                onModuleSelected(moduleClicked);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void onModuleSelected(int moduleClicked) {
        if (moduleClicked==-1)
            return;

        modules[moduleClicked] = ! modules[moduleClicked];
        invalidate();

        moduleStatusAccessibilityHelper.invalidateVirtualView(moduleClicked);
        moduleStatusAccessibilityHelper.sendEventForVirtualView(moduleClicked,AccessibilityEvent.TYPE_VIEW_CLICKED );


    }

    private int findModuleSelected(float x, float y) {
        int moduleClicked = -1;
        for (int i = 0; i < rectangles.length; i++) {
            if (rectangles[i].contains( (int) x , (int) y )){
                moduleClicked = i;
                break;
            }
        }
        return moduleClicked;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int desiredWidth = 0;
        int desiredHeight = 0;

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = specWidth - getPaddingRight() - getPaddingLeft();
        int numberOfModulesThatCanFit = (int) (availableWidth / (shapeWidth+shapeSpacing));
        mMaxHorizontalModules = Math.min( numberOfModulesThatCanFit , modules.length );

        desiredWidth = (int) ( mMaxHorizontalModules * (shapeSpacing+shapeWidth));
        desiredWidth += getPaddingLeft()+getPaddingRight();

        int rows = ( (modules.length - 1) / mMaxHorizontalModules ) + 1;
        desiredHeight = (int) (rows * (shapeSpacing+shapeWidth));
        desiredHeight += getPaddingBottom()+getPaddingTop();

        int width = resolveSizeAndState( desiredWidth , widthMeasureSpec , 0);
        int height = resolveSizeAndState( desiredHeight, heightMeasureSpec , 0);

        setMeasuredDimension( width, height );

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < modules.length; i++) {
            if (shapeValue == 0) {
                float x = rectangles[i].centerX();
                float y = rectangles[i].centerY();
                if (modules[i])
                    canvas.drawCircle(x, y, radius, paintMain);
                canvas.drawCircle(x, y, radius, paintBlack);
            }else {
                drawSquare(canvas,i);
            }
        }

    }

    private void drawSquare(Canvas canvas, int moduleIndex) {
        Rect moduleRectangle = rectangles[moduleIndex];
        if(modules[moduleIndex])
            canvas.drawRect(moduleRectangle, paintMain);
        canvas.drawRect(moduleRectangle , paintBlack );
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        moduleStatusAccessibilityHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return moduleStatusAccessibilityHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return moduleStatusAccessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    public class ModuleStatusAccessibilityHelper extends ExploreByTouchHelper{

        public ModuleStatusAccessibilityHelper(@NonNull View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            int moduleIndex = findModuleSelected(x,y);
            return moduleIndex;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            if (rectangles==null)return;

            for (int i = 0; i < rectangles.length; i++) {
                virtualViewIds.add(i);
            }

        }

        @Override
            protected void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
            // Set the node to focusable so that user can move
            // to a particular module using dpad control
            node.setFocusable(true);
            // Set the focusable area for our current module selected by passing the rectangle
            node.setBoundsInParent(rectangles[virtualViewId]);

            // Sets the content Description for users using talk-back
            node.setContentDescription("Module number " + (virtualViewId+1));

            // Set the node to checkable and set the check property by getting
            // boolean value from the module array at current position
            node.setCheckable(true);
            node.setChecked(modules[virtualViewId]);

            // Says that the node support click action
            node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK);

        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, @Nullable Bundle arguments) {
            switch (action){
                case AccessibilityNodeInfoCompat.ACTION_CLICK:
                    onModuleSelected(virtualViewId);
                    return true;
            }
            return false;
        }
    }

}
