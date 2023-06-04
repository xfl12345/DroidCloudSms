package cc.xfl12345.android.droidcloudsms.model;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * <a href="https://stackoverflow.com/a/64246201/16725063">source code URL</a>
 */
public class OvalTouchAreaFilter implements View.OnTouchListener {

    private boolean clickStart;

    private int loopCount = 0;

    public OvalTouchAreaFilter() {
        clickStart = false;
    }

    public boolean isInTouchArea(View view, MotionEvent event) {
        int w = view.getWidth();
        int h = view.getHeight();
        if (w <= 0 || h <= 0)
            return false;
        float xhat = 2 * event.getX() / w - 1;
        float yhat = 2 * event.getY() / h - 1;
        return (xhat * xhat + yhat * yhat <= 1);
    }

    private boolean performTouchToParent(View view, MotionEvent event) {
        if (view.getParent() instanceof View) {
            View parentView = (View) view.getParent();
            synchronized (OvalTouchAreaFilter.class) {
                loopCount += 1;
                if (loopCount > 3) {
                    loopCount -= 1;
                    return true;
                }
            }
            // MotionEvent moved = MotionEvent.obtain(event);
            // MotionEvent moved = MotionEvent.obtain(
            //     event.getDownTime(),
            //     event.getEventTime(),
            //     event.getAction(),
            //     view.getX() + event.getX(),
            //     view.getY() + event.getY(),
            //     event.getMetaState()
            // );
            // MotionEvent moved = MotionEvent.obtain(
            //     event.getDownTime(),
            //     event.getEventTime(),
            //     event.getAction(),
            //     event.getRawX(),
            //     event.getRawY(),
            //     event.getMetaState()
            // );
            // moved.setLocation(view.getX() * 2 + event.getX() * 2, view.getY() * 2 + event.getY() * 2);
            // moved.setLocation(view.getX() + event.getX(), view.getY() + event.getY());
            // moved.setLocation(view.getX() + event.getX(), view.getY() + event.getY() * 2);
            // moved.setLocation(view.getX(), view.getY());
            // moved.setLocation(event.getRawX(), event.getRawY());
            // moved.offsetLocation(view.getX(), view.getY());
            // Log.d("点击测试", "原始绝对坐标：" + String.format("X=%s,Y=%s", event.getRawX(), event.getRawY()));
            // Log.d("点击测试", "修改坐标：" + String.format("X=%s,Y=%s", moved.getX(), moved.getY()));
            // parentView.dispatchTouchEvent(event);
            // moved.recycle();
            parentView.performClick();

            synchronized (OvalTouchAreaFilter.class) {
                loopCount -= 1;
            }
        }

        return true;
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getActionMasked();
        boolean consume = false;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (isInTouchArea(view, event)) {
                    clickStart = true;
                    consume = true;
                } else {
                    consume = performTouchToParent(view, event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                consume = isInTouchArea(view, event);
                break;
            case MotionEvent.ACTION_UP:
                boolean historyClickStart = clickStart;
                clickStart = false;
                if (historyClickStart && isInTouchArea(view, event)) {
                    view.performClick();
                }
                consume = true;
                break;
            case MotionEvent.ACTION_CANCEL:
                clickStart = false;
                consume = true;
                break;
            default:
                break;
        }

        return consume;
    }

}
