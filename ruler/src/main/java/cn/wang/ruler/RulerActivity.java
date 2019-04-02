package cn.wang.ruler;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import cn.router.werouter.annotation.Router;

@Router(path = "native://RulerActivity")
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
