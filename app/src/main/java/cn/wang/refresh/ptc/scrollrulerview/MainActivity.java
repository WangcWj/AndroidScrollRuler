package cn.wang.refresh.ptc.scrollrulerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.router.api.router.WeRouter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WeRouter.init(getApplication());
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //跨module通讯的路由 这里就小试一下
                WeRouter.getInstance().build("native://RulerActivity").navigation(MainActivity.this);
            }
        });
    }
}
