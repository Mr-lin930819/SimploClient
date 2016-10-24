package com.localhost.lin.simploc

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import com.localhost.lin.simploc.SQLite.SQLiteOperation

class SettingActivity : AppCompatActivity() {

    private var sqLiteOperation: SQLiteOperation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        sqLiteOperation = SQLiteOperation(this)
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        actionBar.setTitle("设置")
        val deleteUserButton = findViewById(R.id.delete_user_btn) as Button?
        deleteUserButton!!.setOnClickListener {
            val dialog = AlertDialog.Builder(this@SettingActivity).setTitle("删除账号").setMessage("确定删除吗？").setPositiveButton("确定") { dialog, which ->
                sqLiteOperation!!.delete(NetworkThreads.loginInfo!!.number)
                val intent = Intent()
                intent.putExtra("action", "logout")
                setResult(Activity.RESULT_OK, intent)
                finish()
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }.setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
        }

        val checkBox = findViewById(R.id.is_show_avator) as CheckBox?
        val checked = sqLiteOperation!!.queryIsShowAvator(NetworkThreads.loginInfo!!.number)
        checkBox!!.isChecked = checked
        checkBox.setOnClickListener(View.OnClickListener { sqLiteOperation!!.updateIsShowAvator(NetworkThreads.loginInfo!!.number, checkBox.isChecked) })
    }

    override fun onBackPressed() {
        //super.onBackPressed();
        val intent = Intent()
        intent.putExtra("action", "refreshAvator")
        setResult(Activity.RESULT_OK, intent)
        finish()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent()
                intent.putExtra("action", "refreshAvator")
                setResult(Activity.RESULT_OK, intent)
                finish()
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                return true
            }
            else -> {
            }
        }
        return false
    }
}
