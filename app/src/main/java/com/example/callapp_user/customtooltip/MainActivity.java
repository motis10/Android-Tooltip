package com.example.callapp_user.customtooltip;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private ToolTipPopup toolTipPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolTipPopup = ToolTipPopup.createToolTip(button, "Hi click here for more info", null, 0);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            dismissTooltilIfNeeded();
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        dismissTooltilIfNeeded();
        super.onDestroy();
    }

    private void dismissTooltilIfNeeded() {
        if (toolTipPopup != null && toolTipPopup.isTooltipShowing()) {
            toolTipPopup.dismiss(false);
        }
    }
}
