package com.example.simplefitness

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject
import splitties.alertdialog.alertDialog
import splitties.alertdialog.okButton
import splitties.toast.longToast
import splitties.toast.toast
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.PrintStream

class MainActivity : AppCompatActivity() {

    private val DATA_FILENAME = "FitnessData"
    private var fitnessList = arrayListOf<String>()

    private val TAG = "MainActivity"
    private val mURL = "http://draussenwetter.appspot.com/MCdata.json"

    private val btnLoad : Button by lazy{ findViewById(R.id.button_loadData) }
    private val datatv : TextView by lazy{ findViewById(R.id.dataTV) }

    private val mHandler : Handler by lazy { Handler() }
    private lateinit var mRunnable : Runnable

    private val mRequestQueue : RequestQueue by lazy { Volley.newRequestQueue(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var loading = false

        btnLoad.setOnClickListener {
            Log.i(TAG, "Button LadeDaten wurde gedrückt")
            loading = !loading
            btnLoad.text = if(loading) getString(R.string.stop_data)
            else getString(R.string.load_data)
            liveData(loading)
        }

    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop")
        saveToFile()
        liveData(false)
    }

    private fun getDataFromInternet() {
        val mStringRequest = StringRequest(Request.Method.GET, mURL,
                Response.Listener {
                    parseJSONData(it)
                    fitnessList.add(it)
                    if(fitnessList.size >= 10) {
                        saveToFile()
                    }
                }, Response.ErrorListener {
            dialogError(getString(R.string.error_internet_communication))
        })

        mRequestQueue.add(mStringRequest)
    }

    private fun parseJSONData(jsonString : String) {
        try {
            //response String zu einem JSON Objekt
            val obj = JSONObject(jsonString)
            val pulse = obj.getInt("puls")

            toast("Puls: $pulse")
            datatv.text = pulse.toString()
        } catch (e : JSONException) {
            e.printStackTrace()
            Log.e(TAG, getString(R.string.error_json_parsing))
            dialogError(getString(R.string.error_json_parsing))
        }
    }

    private fun dialogError(message : String) {
        alertDialog(title = getString(R.string.error_msg_title), message = message) {
            okButton{
                finish()
            }
        }.show()
    }

    private fun liveData(loading : Boolean) {
        if (loading) {
            mRunnable = Runnable {
                mHandler.postDelayed(mRunnable, 3 * 1000)
                getDataFromInternet()
            }
            mHandler.postDelayed(mRunnable, 100)
        } else {
            mHandler.removeCallbacks(mRunnable)
        }
    }

    private fun saveToFile() {
        Log.i(TAG, "Save Data to File")

        var fileOutStream : FileOutputStream? = null

        try {
            fileOutStream = openFileOutput(DATA_FILENAME, Context.MODE_PRIVATE or Context.MODE_APPEND)
        }
        catch (e: FileNotFoundException) {
            e.printStackTrace()
            dialogError(getString(R.string.error_file_not_found))
        }

        val printStream = PrintStream(fileOutStream)
        for(data in fitnessList){
            printStream.println(data)
        }
        if(printStream.checkError()) {
            dialogError(getString(R.string.error_saving_file))
        }
        printStream.close()

        fitnessList.clear()
    }
}