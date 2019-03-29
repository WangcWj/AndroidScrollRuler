package cn.wang.refresh.ptc.scrollrulerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    ScrollRulerLayout rulerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rulerView = findViewById(R.id.ruler_view);
        rulerView.setScope(5000,15001);
        rulerView.setCurrentItem("10000");
    }
}
