package com.desert.router

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.desert.router.annotations.Destination

@Destination(url = "router://test", description = "kotlin测试")
class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_test)
        var textView:TextView=findViewById(R.id.tv)
        textView.text=intent.getStringExtra("name")
//        val textView: TextView = findViewById(R.id.tv)
//        textView.setOnClickListener {
//            if (intent != null) {
//                (it as TextView).text = intent.extras.toString()
//            }
//        }
    }

}