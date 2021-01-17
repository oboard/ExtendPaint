package oboard.ep

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.UnsupportedEncodingException
import java.net.URLEncoder

class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout).title = title
        findViewById<FloatingActionButton>(R.id.fab_about).setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW
            )
            var str = ""
            try {
                str = "alipays://platformapi/startapp?saId=10000007&qrcode=" + URLEncoder.encode(
                    "https://qr.alipay.com/tsx00700h1zpzwoodysyhda",
                    "UTF-8"
                )
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
            //str是支付宝或微信链接，直接在微信里面点击链接，也可调起
            val uri = Uri.parse(str)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.data = uri
            startActivity(intent)
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
}