package com.localhost.lin.simploc

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter

import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

import com.localhost.lin.simploc.Entity.UserEntity
import com.localhost.lin.simploc.Fragments.CETFragment
import com.localhost.lin.simploc.Fragments.CourseTableFragment
import com.localhost.lin.simploc.Fragments.ExamTimeTableFragment
import com.localhost.lin.simploc.Fragments.GradeFragment
import com.localhost.lin.simploc.SQLite.SQLiteOperation
import com.localhost.lin.simploc.Utils.ImageUtils
import com.localhost.lin.simploc.Utils.JsonUtils
import com.localhost.lin.simploc.Utils.NetworkUrlUtils
import com.localhost.lin.simploc.customview.MaskImage
import com.localhost.lin.simploc.query_interface.ThesisApi
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.RequestParams
import com.loopj.android.http.TextHttpResponseHandler

import org.apache.http.client.methods.HttpGetHC4
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtilsHC4

import java.io.IOException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

import cz.msebera.android.httpclient.Header
import kotlinx.android.synthetic.main.nav_header_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    internal var threads: NetworkThreads? = null
    //WebView resultWebview;
    internal var toxiang: MaskImage? = null
    internal var nameText: TextView? = null
    internal var sqLiteOperation: SQLiteOperation? = null
    internal var userInfo: UserEntity? = null
    //TabHost tabHost;
    internal var tabHost: LinearLayout? = null
    private var progressDialog: ProgressDialog? = null

    private val thesisService = CustomApplication.getInstance().retrofit.create(ThesisApi::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sqLiteOperation = SQLiteOperation(this)
        setContentView(R.layout.activity_main)
        //载入已登录的用户信息，此处学号获取采用静态全局变量的方式，更好的方式是采用意图Intent
        // UserInfo为已登录用户信息，将会存在于整个MainActivity生命周期内，完成各项查询
        userInfo = sqLiteOperation!!.findUser(NetworkThreads.loginInfo.number)
        val toolbar = findViewById(R.id.toolbar) as Toolbar?
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton?
        fab!!.setOnClickListener { view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show() }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout?
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer!!.setDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById(R.id.nav_view) as NavigationView?
        navigationView!!.setNavigationItemSelectedListener(this)

        threads = NetworkThreads(handler)

        /**
         * 生成左边抽屉框的展示数据
         * 包括学号、姓名、专业以及头像照片
         */
        val navHeader = navigationView.inflateHeaderView(R.layout.nav_header_main)
        nameText = navHeader.findViewById(R.id.navbar_name_text) as TextView
        val numberText = navHeader.findViewById(R.id.nav_number_text) as TextView
        toxiang = navHeader.findViewById(R.id.imageView) as MaskImage
        numberText.text = userInfo!!.number

        //获取专业名称
        loadMajorName()
        //获取头像图片
        if (sqLiteOperation!!.queryIsShowAvator(userInfo!!.number))
            AvatarGetTask(userInfo!!.number, userInfo!!.cookie).execute(null as Void)

        //默认主界面显示课程表    2016.01.27 Add
        val cstbData = sqLiteOperation!!.getSavedCsTb(userInfo!!.number)
        if (cstbData != "") {
            showCourseTable(cstbData, 10)
        }

    }

    /**
     * 联网获得专业名称
     */
    private fun loadMajorName() {

        val loadService = thesisService.loadMajorName(userInfo!!.openAppId)
        loadService.enqueue(object : Callback<String> {
            override fun onFailure(call: Call<String>?, t: Throwable?) {

            }

            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                val majorText = JsonUtils.getNodeString(
                        response?.body().toString(), "ZY")//获取专业信息
                //nameText.setText(NetworkThreads.loginInfo.getXm() + "\t\t" + retData);
                navbar_name_text?.text = userInfo!!.name + "\t\t" + majorText
            }

        })

//        val client = AsyncHttpClient()
//        client.get(NetworkUrlUtils.XN_OPTIONS_URL, RequestParams(object : HashMap<String, String>() {
//            init {
//                //                put("number", userInfo.getNumber());
//                //                put("xm", userInfo.getName());
//                //                put("cookie", userInfo.getCookie());
//                put("openUserId", userInfo!!.openAppId)
//            }
//        }), object : TextHttpResponseHandler() {
//            override fun onStart() {
//                charset = "gb2312"
//                super.onStart()
//            }
//
//            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {
//
//            }
//
//            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
//                var majorText: String? = null
//                majorText = JsonUtils.getNodeString(s, "ZY")//获取专业信息
//                //nameText.setText(NetworkThreads.loginInfo.getXm() + "\t\t" + retData);
//                nameText?.text = userInfo!!.name + "\t\t" + majorText
//            }
//        })
    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout?
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            AlertDialog.Builder(this@MainActivity).setTitle("退出应用").setMessage("确定退出吗？").setPositiveButton("确定") { dialog, which -> android.os.Process.killProcess(android.os.Process.myPid()) }.setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
        } else if (id == R.id.action_about) {
            AlertDialog.Builder(this@MainActivity).setTitle("关于").setMessage("SimploC    1.0.6 \n\nAuthor: Lin \n" +
                    "Technical Support: Tom Zhang \n\n " +
                    //"\n 应用使用的开源框架/库: \n - AsyncHttpClient (异步网络请求库)" +
                    //"\n - ViewPagerIndicator (ViewPager指示器)" +
                    "\n 应用使用的开源框架/库: \n - AsyncHttpClient " +
                    "\n - ViewPagerIndicator " +
                    "\n - Chart.js (基于Html5 Canvas的图表绘制库)" +
                    "\n - LitePay (SQLite数据库ORM)" +
                    "\n - org.json (JSON解析工具)" +
                    "\n\n 2016.05.06").show()
        }
        //        }else if (id == R.id.action_exit) {
        //            return true;
        //        }

        return super.onOptionsItemSelected(item)
    }

    @SuppressWarnings("StatementWithEmptyBody")
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId

        if (id == R.id.nav_exam) {
            // Handle the camera action
            //查询考试信息
            loadOption(QUERY_CTRL.QUERY_EXAM)
        } else if (id == R.id.nav_cet) {
            loadOption(QUERY_CTRL.QUERY_CET)
            //        } else if (id == R.id.nav_slideshow) {
            //            resultWebview.loadUrl("file:///android_asset/wait_page.html");
            //            new Thread(threads.new QueryGradeThread("2014-2015", "",sqLiteOperation)).start();
        } else if (id == R.id.nav_grade) {
            val intent = Intent()
            intent.setClass(this@MainActivity, SelectTimeActivity::class.java)
            startActivityForResult(intent, CUSTOM_QUERY_REQUEST_CODE)
        } else if (id == R.id.query_lesson) {
            loadOption(QUERY_CTRL.QUERY_LESSON)       //弹出时间选择对话框,启动课程表查询
        } else if (id == R.id.nav_gpa_info) {
            val intent = Intent()
            intent.setClass(this@MainActivity, CreditStatActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.nav_share) {
            //resultWebview.loadUrl("file:///android_asset/wait_page.html");
            startActivityForResult(Intent().setClass(this@MainActivity, SettingActivity::class.java), SETTING_REQUEST_CODE)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out)
        } else if (id == R.id.nav_send) {
            val dialog = AlertDialog.Builder(this@MainActivity).setTitle("登出").setMessage("确定登出吗？").setPositiveButton("确定") { dialog, which -> logout() }.setNegativeButton("取消") { dialog, which -> dialog.dismiss() }.show()
        }

        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout?
        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    private enum class QUERY_CTRL {
        QUERY_LESSON,
        QUERY_EXAM,
        QUERY_CET
    }

    /**
     * 载入查询的学期学年选择对话框
     * @param func  要查询功能（课程表、考试信息等）
     */
    private fun loadOption(func: QUERY_CTRL) {
        val layout = layoutInflater.inflate(R.layout.dialog_select, null) as LinearLayout
        val xnSpinner = layout.findViewById(R.id.dialog_xn_spinner) as Spinner
        val xqSpinner = layout.findViewById(R.id.dialog_xq_spinner) as Spinner
        val weekSpinner = layout.findViewById(R.id.dialog_week_spinner) as Spinner
        val optUrl: String

        if (func == QUERY_CTRL.QUERY_CET) {
            queryCET()
            return
        }

        if (func == QUERY_CTRL.QUERY_LESSON) {
            optUrl = NetworkUrlUtils.TB_XN_OP_URL
        } else {
            optUrl = NetworkUrlUtils.XN_OPTIONS_URL
        }

        val httpClient = AsyncHttpClient()
        val params = RequestParams(object : HashMap<String, String>() {
            init {
                //                put("number",userInfo.getNumber());
                //                put("xm",userInfo.getName());
                //                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo!!.openAppId)
            }
        })

        val dialog = ProgressDialog.show(this@MainActivity, "获取选项",
                "正在获取选项信息，请稍后...", true, true) { httpClient.cancelAllRequests(true) }
        httpClient.get(optUrl, params, object : TextHttpResponseHandler() {

            override fun onStart() {
                charset = "gb2312"
                super.onStart()
            }

            override fun onFailure(i: Int, headers: Array<cz.msebera.android.httpclient.Header>, s: String, throwable: Throwable) {
                dialog.dismiss()
                Toast.makeText(this@MainActivity, "网络请求出错", Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(i: Int, headers: Array<cz.msebera.android.httpclient.Header>, s: String) {

                dialog.dismiss()
                val retData = JsonUtils.convJson2List(s, "CXTJ")
                if (retData == null) {
                    Toast.makeText(this@MainActivity, "获取学年失败(服务器错误或网络错误", Toast.LENGTH_LONG).show()
                    return
                }
                val xnAdapter = ArrayAdapter(layout.context,
                        android.R.layout.simple_spinner_item, retData)
                val xqAdapter = ArrayAdapter(layout.context, android.R.layout.simple_spinner_item,
                        object : ArrayList<String>() {
                            init {
                                add("")
                                add("第一学期")
                                add("第二学期")
                                add("第三学期")
                            }
                        })
                val weekAdapter: ArrayAdapter<*>

                xnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                xnSpinner.adapter = xnAdapter
                xnSpinner.setSelection(1)

                xqAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                xqSpinner.adapter = xqAdapter
                xqSpinner.setSelection(1, true)

                //如果是课程表查询，则需要加载周数选择
                if (func == QUERY_CTRL.QUERY_LESSON) {
                    val weekData = ArrayList<String>()
                    for (j in 0..17) {
                        weekData.add("第" + (j + 1).toString() + "周")
                    }
                    weekAdapter = ArrayAdapter(layout.context,
                            android.R.layout.simple_spinner_item, weekData)
                    weekSpinner.adapter = weekAdapter
                    weekSpinner.visibility = View.VISIBLE
                }

                AlertDialog.Builder(this@MainActivity).setTitle("选择要查询的学年或学期").setView(layout).setPositiveButton("查询") { dialog, which ->
                    when (func) {
                        MainActivity.QUERY_CTRL.QUERY_LESSON -> queryLesson(xnSpinner.selectedItem.toString(),
                                xqSpinner.selectedItemPosition.toString(),
                                (weekSpinner.selectedItemPosition + 1).toString())
                        MainActivity.QUERY_CTRL.QUERY_EXAM -> queryExam(xnSpinner.selectedItem.toString(),
                                xqSpinner.selectedItemPosition.toString())
                        else -> {
                        }
                    }
                }.setNegativeButton("取消", null).show()
            }
        })
    }


    /**
     * 启动课程表查询网络请求
     * @param xn    要查询的学年
     * *
     * @param xq    要查询的学期
     * *
     * @param week  要查询的周数[附加功能]
     */
    private fun queryLesson(xn: String, xq: String, week: String) {
        val dialog = ProgressDialog.show(this@MainActivity, "课程表", "查询中... ...")

        //        //测试
        //        if(false) {
        //            TestNetwork testNetwork = new TestNetwork(NetworkUrlUtils.TEST_LESSON_URL,"?number=" + userInfo.getNumber() + "&name=" + userInfo.getName() + "&cookie=" +
        //                    userInfo.getCookie() + "&xn=" + xn + "&xq=" + xq);
        //            new Thread(testNetwork).start();
        //            dialog.dismiss();
        //            return;
        //        }

        val lessonGet = thesisService.queryLesson(userInfo!!.openAppId, xn, xq, week)
        lessonGet.enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>?, response: Response<String>?) {
                Log.d("Success!!", userInfo!!.number)
                dialog.dismiss()
                if (sqLiteOperation?.getSavedCsTb(userInfo!!.number) == "") {
                    sqLiteOperation?.insertCourseTb(userInfo!!.number, response?.body().toString())
                } else {
                    sqLiteOperation?.updateCsTb(userInfo!!.number, response?.body().toString())
                }
                showCourseTable(response?.body().toString(), Integer.parseInt(week))
            }

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                dialog.dismiss()
                Toast.makeText(this@MainActivity, "成绩查询失败", Toast.LENGTH_LONG).show()
            }

        })

//        val networkManager = AsyncHttpClient()
//        val params = RequestParams(object : HashMap<String, String>() {
//            init {
//                //                put("number",userInfo.getNumber());
//                //                put("name",userInfo.getName());
//                //                put("cookie",userInfo.getCookie());
//                put(NetworkUrlUtils.RQ_K_OPENID, userInfo!!.openAppId)
//                put("xn", xn)
//                put("xq", xq)
//                put("week", week)
//            }
//        })
//        networkManager.get(NetworkUrlUtils.LESSON_URL, params, object : TextHttpResponseHandler() {
//            override fun onStart() {
//                super.onStart()
//                charset = "gb2312"
//            }
//
//            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {
//                dialog.dismiss()
//                Toast.makeText(this@MainActivity, "成绩查询失败", Toast.LENGTH_LONG).show()
//            }
//
//            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
//                Log.d("Success!!", userInfo!!.number)
//                dialog.dismiss()
//                if (sqLiteOperation?.getSavedCsTb(userInfo!!.number) == "") {
//                    sqLiteOperation?.insertCourseTb(userInfo!!.number, s)
//                } else {
//                    sqLiteOperation?.updateCsTb(userInfo!!.number, s)
//                }
//                showCourseTable(s, Integer.parseInt(week))
//            }
//        })
    }

    /**
     * 启动考试时间表查询的网络请求
     * @param xn    查询的学年
     * *
     * @param xq    查询的学期
     */
    private fun queryExam(xn: String, xq: String) {
        val dialog = ProgressDialog.show(this@MainActivity, "考试信息", "查询中... ...")
        val networkManager = AsyncHttpClient()
        val params = RequestParams(object : HashMap<String, String>() {
            init {
                //                put("number",userInfo.getNumber());
                //                put("name",userInfo.getName());
                //                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo!!.openAppId)
                put("xn", xn)
                put("xq", xq)
            }
        })
        networkManager.get(NetworkUrlUtils.EXAM_URL, params, object : TextHttpResponseHandler() {
            override fun onStart() {
                super.onStart()
                charset = "gb2312"
            }

            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {
                Log.e("Fail~~", NetworkUrlUtils.EXAM_URL)
                dialog.dismiss()
                Toast.makeText(this@MainActivity, "请求数据出错，重新登陆或稍后重试.,", Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
                showExamTimeTable(s)
                dialog.dismiss()
            }
        })
    }

    /**
     * 查询等级信息的网络请求
     */
    private fun queryCET() {
        val dialog = ProgressDialog.show(this@MainActivity, "等级考试信息", "查询中... ...")
        val networkManager = AsyncHttpClient()
        val params = RequestParams(object : HashMap<String, String>() {
            init {
                //                put("number",userInfo.getNumber());
                //                put("name",userInfo.getName());
                //                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo!!.openAppId)
            }
        })
        networkManager.get(NetworkUrlUtils.CET_URL, params, object : TextHttpResponseHandler() {
            override fun onStart() {
                super.onStart()
                charset = "gb2312"
            }

            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {
                Log.d("Fail~~", NetworkUrlUtils.EXAM_URL)
                dialog.dismiss()
                Toast.makeText(this@MainActivity, "请求数据出错，重新登陆或稍后重试.,", Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
                dialog.dismiss()
                if (s == "CODE2") {//需要进行一键评价
                    AlertDialog.Builder(this@MainActivity).setTitle("一键评价").setMessage("需要进行一键评价才能继续，确定进行一键评价吗？").setPositiveButton("确定") { dialog, which -> processOneKeyComment(s, QUERY_CTRL.QUERY_CET, 0) }.setNegativeButton("取消", null).show()
                    return
                } else if (s == "CODE1") {
                    Toast.makeText(this@MainActivity, "身份信息超时，需重新登陆", Toast.LENGTH_LONG).show()
                    return
                }
                showCETTable(s)
            }
        })
    }

    private fun processOneKeyComment(s: String, func: QUERY_CTRL, week: Int) {
        val networkManager = AsyncHttpClient()
        val params = RequestParams(object : HashMap<String, String>() {
            init {
                //                put("number",userInfo.getNumber());
                //                put("name",userInfo.getName());
                //                put("cookie",userInfo.getCookie());
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo!!.openAppId)
            }
        })
        val dialog = ProgressDialog.show(this@MainActivity, "一键评价", "一键评价进行中...")
        networkManager.get(NetworkUrlUtils.ONE_KEY_COMMENT, params, object : TextHttpResponseHandler() {
            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {

            }

            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
                dialog.dismiss()
                Toast.makeText(this@MainActivity, "一键评价成功!", Toast.LENGTH_LONG).show()
            }
        })
    }

    /**
     * 根据Json内容显示课程表信息到课程表视图中
     * @param jsonContent 传入的Json数据
     */
    private fun showCourseTable(jsonContent: String, week: Int) {
        supportActionBar!!.title = "课程表"
        supportActionBar!!.subtitle = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(java.util.Date())
        val courseTableView = CourseTableFragment.newInstance(jsonContent, week.toString())
        switchToView(courseTableView)
    }

    /**
     * 根据Json内容显示考试时间表到List中
     * @param jsonContent 传入的Json数据
     */
    private fun showExamTimeTable(jsonContent: String?) {
        if (jsonContent == null || jsonContent == "") {
            Toast.makeText(this@MainActivity, "从服务器收到的数据有误", Toast.LENGTH_SHORT).show()
            return
        }
        supportActionBar!!.title = "考试时间表"
        supportActionBar!!.subtitle = SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA).format(java.util.Date())
        val timeTableView = ExamTimeTableFragment.newInstance(jsonContent)
        switchToView(timeTableView)
    }

    private fun showCETTable(jsonContent: String) {
        supportActionBar!!.title = "等级考试成绩单"
        supportActionBar!!.subtitle = ""
        val cetFragment = CETFragment.newInstance(jsonContent)
        switchToView(cetFragment)
    }

    private fun switchToView(view: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.main_page, view)
        transaction.commit()
    }

    private fun logout() {
        val httpClient = AsyncHttpClient()
        val params = object : RequestParams() {
            init {
                put(NetworkUrlUtils.RQ_K_OPENID, userInfo!!.openAppId)
            }
        }
        httpClient.get(NetworkUrlUtils.LOGOUT, params, object : TextHttpResponseHandler() {
            override fun onFailure(i: Int, headers: Array<Header>, s: String, throwable: Throwable) {

            }

            override fun onSuccess(i: Int, headers: Array<Header>, s: String) {
                val intent = Intent()
                intent.setClass(this@MainActivity, LoginActivity::class.java)
                //设置数据库中登录状态为登出
                sqLiteOperation?.updateLoginStatus(NetworkThreads.loginInfo.number, "0")
                startActivity(intent)
                finish()
            }
        })
    }

    //后台获取头像照片的任务
    private inner class AvatarGetTask(private val mNumber: String, private val mCookie: String) : AsyncTask<Void, Void, Bitmap>() {

        override fun doInBackground(vararg params: Void): Bitmap {
            val netManager = HttpClients.createDefault()
            var retImage: Bitmap? = null
            //            HttpGetHC4 gradeQueryGetRequest = new HttpGetHC4(NetworkUrlUtils.AVATOR_URL + "?number=" + mNumber +
            //                    "&cookie=" + mCookie);
            /*
             *   2015-10-16 从服务端获取头像改为直接从网站获取头像
             */
            val gradeQueryGetRequest = HttpGetHC4("http://jwgl.fjnu.edu.cn/readimagexs.aspx?xh=" + userInfo!!.number)
            gradeQueryGetRequest.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            gradeQueryGetRequest.setHeader("Connection", "Keep-Alive")
            gradeQueryGetRequest.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36")
            gradeQueryGetRequest.setHeader("Cookie", mCookie)
            gradeQueryGetRequest.setHeader("Upgrade-Insecure-Requests", "1")
            gradeQueryGetRequest.setHeader("Accept-Encoding", "gzip, deflate, sdch")

            var result: ByteArray? = null
            //HashMap<String,String> gradeList = new HashMap<String,String>();
            try {
                result = EntityUtilsHC4.toByteArray(netManager.execute(gradeQueryGetRequest).entity)
                //gradeList = convJson2Map(result,"GRADE");
            } catch (e: IOException) {
                e.printStackTrace()
            }

            Log.d("MainActivity 315:", result!!.size.toString())

            retImage = BitmapFactory.decodeByteArray(result, 0, result.size)
            retImage = ImageUtils.ImageCrop(retImage)
            retImage = ImageUtils.scaleImage(retImage, 72, 72)
            return retImage
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            super.onPostExecute(bitmap)
            if (bitmap == null) {
                return
            }
            //获取到头像后设置头像到界面
            toxiang?.setMaskBitmap(bitmap, BitmapFactory.decodeResource(resources, R.drawable.mask))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTING_REQUEST_CODE) {//设置
            //如果要求（删除用户后）登出：
            if (resultCode == Activity.RESULT_OK && data.getStringExtra("action") == "logout") {
                //                Intent intent = new Intent();
                //                intent.setClass(MainActivity.this, LoginActivity.class);
                //                startActivity(intent);
                //                finish();
                logout()
            } else if (resultCode == Activity.RESULT_OK && data.getStringExtra("action") == "refreshAvator") {
                //获取头像图片
                if (sqLiteOperation?.queryIsShowAvator(userInfo!!.number)!!)
                    AvatarGetTask(userInfo!!.number, userInfo!!.cookie).execute(null as Void)
                else
                    toxiang?.setMaskBitmap(BitmapFactory.decodeResource(resources, R.drawable.avatar),
                            BitmapFactory.decodeResource(resources, R.drawable.mask))
            }
        } else if (requestCode == CUSTOM_QUERY_REQUEST_CODE) {//自定义查询成绩
            if (resultCode == Activity.RESULT_OK) {
                supportActionBar!!.setTitle("成绩单")
                Log.i("Grade", "学年：" + data.getStringExtra("xn") + "  " + data.getStringExtra("xq"))
                //resultWebview.loadUrl("file:///android_asset/wait_page.html");
                progressDialog = ProgressDialog.show(this@MainActivity, "成绩单", "查询中... ...")
                Thread(threads!!.QueryGradeThread(data.getStringExtra("xn"), data.getStringExtra("xq"), sqLiteOperation)).start()
            }
        }
    }

    /**
     * 用于处理消息
     */
    private val handler = object : Handler(Looper.myLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if ((msg.obj as String).equals("queryGradeFinished", ignoreCase = true)) {
                //                Bundle bundle = msg.getData();
                //                for(String key:bundle.keySet()){
                //
                //                }
                //                RelativeLayout layout = (RelativeLayout) findViewById(R.id.main_page);
                //                layout.removeAllViews();
                //                View view = getLayoutInflater().inflate(R.layout.activity_result_page, null);

                //                RelativeLayout resultView = (RelativeLayout) view.findViewById(R.id.result_view);
                //WebView resultWebview = (WebView) findViewById(R.id.result_web_view);

                val resultJson = msg.data.getString("json")
                val gradeView = GradeFragment.newInstance(resultJson)
                progressDialog!!.dismiss()
                switchToView(gradeView)
            }
        }
    }

    companion object {

        private val SETTING_REQUEST_CODE = 1
        private val CUSTOM_QUERY_REQUEST_CODE = 2
    }
}
