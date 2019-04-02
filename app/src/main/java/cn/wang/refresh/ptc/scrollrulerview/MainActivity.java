package cn.wang.refresh.ptc.scrollrulerview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import cn.router.api.router.WeRouter;
import cn.wang.ruler.RulerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        WeRouter.init(getApplication());
        findViewById(R.id.btn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RulerActivity.class);
                startActivity(intent);
            }
        });
    }
}
