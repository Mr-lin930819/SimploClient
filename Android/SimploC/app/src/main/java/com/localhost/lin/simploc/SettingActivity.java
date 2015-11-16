package com.localhost.lin.simploc;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.localhost.lin.simploc.SQLite.SQLiteOperation;

public class SettingActivity extends AppCompatActivity {

    private SQLiteOperation sqLiteOperation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        sqLiteOperation = new SQLiteOperation(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("设置");
        Button deleteUserButton = (Button)findViewById(R.id.delete_user_btn);
        deleteUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqLiteOperation.delete(NetworkThreads.loginInfo.getNumber());
                Intent intent = new Intent();
                intent.putExtra("action", "logout");
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });

        final CheckBox checkBox = (CheckBox)findViewById(R.id.is_show_avator);
        final boolean checked = sqLiteOperation.queryIsShowAvator(NetworkThreads.loginInfo.getNumber());
        checkBox.setChecked(checked);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sqLiteOperation.updateIsShowAvator(NetworkThreads.loginInfo.getNumber(),checkBox.isChecked());
            }
        });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent=new Intent();
        intent.putExtra("action", "refreshAvator");
        setResult(RESULT_OK, intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent=new Intent();
                intent.putExtra("action", "refreshAvator");
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                return true;
            default:
                break;
        }
        return false;
    }
}
