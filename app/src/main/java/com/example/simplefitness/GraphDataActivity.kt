package com.example.simplefitness

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.json.JSONException
import org.json.JSONObject
import splitties.alertdialog.alertDialog
import splitties.alertdialog.okButton
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader

class GraphDataActivity : AppCompatActivity() {
    private val TAG = "GraphDataActivity"
    private val DATA_FILENAME = "FitnessData"

    private val graph : GraphView by lazy { findViewById(R.id.graph) }

    private var pulseSeries = LineGraphSeries<DataPoint>()
    private var lastXvalue : Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_data)

        getDataFromFile()

    }

    private fun getDataFromFile() {
        var fileInput : FileInputStream? = null
        try {
            fileInput = openFileInput(DATA_FILENAME)
            val bufferdIn = BufferedReader(InputStreamReader(fileInput))
            var data = bufferdIn.readLine()
            while(data != null) {
                parseJSON(data)
                data = bufferdIn.readLine()
            }
            bufferdIn.close()
            graph.addSeries(pulseSeries)
        }
        catch (e : Exception) {
            e.printStackTrace()
            dialogError(e.localizedMessage)
        }
    }

    private fun parseJSON(jsonString : String) {
        try {
            val obj = JSONObject(jsonString)
            val pulse = obj.getInt("puls").toDouble()
            Log.i(TAG, "Puls: $pulse")

            pulseSeries.appendData(DataPoint(lastXvalue, pulse), true, 1000)
            lastXvalue++
        }
        catch (e : JSONException) {
            Log.e(TAG, "Error JSON Parsing")
            e.printStackTrace()
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
}