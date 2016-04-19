package nl.qrk.currencytextviewdemo

import android.app.Activity
import android.content.Context
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper
import java.math.BigDecimal

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view1.setAmount(BigDecimal(System.currentTimeMillis()/100))
    }

    override fun attachBaseContext(newBase : Context){
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase))
    }
}
