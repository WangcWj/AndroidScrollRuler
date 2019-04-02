package cn.wang.ruler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * @author WANG
 *  GitHub -> https://github.com/WangcWj/AndroidScrollRuler
 *
 *  提交issues联系作者.
 */
public class RulerActivity extends AppCompatActivity {
    ScrollRulerLayout rulerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruler);
        rulerView = findViewById(R.id.ruler_view);
        rulerView.setScope(5000,15001,500);
        rulerView.setCurrentItem("10000");
    }
}
