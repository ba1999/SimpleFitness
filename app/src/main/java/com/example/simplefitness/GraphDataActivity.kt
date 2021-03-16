package com.example.simplefitness

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.json.JSONException
import org.json.JSONObject
import splitties.alertdialog.alertDialog
import splitties.alertdialog.okButton
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class GraphDataActivity : AppCompatActivity() {
    private val TAG = "GraphDataActivity"
    private val DATA_FILENAME = "FitnessData"

    private val graph : GraphView by lazy { findViewById(R.id.graph) }


    private var pulseSeries = BarGraphSeries<DataPoint>()
    private var lastXvalue : Double = 0.0
    private var dataArray = arrayListOf<String>()
    private val dataPoints : Array<DataPoint> by lazy { Array(dataArray.size, { DataPoint(0.0, 0.0) }) }
    private var last : Date = Calendar.getInstance().time

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_data)

        getDataFromFile()
        parseJSONToSeries()
        configGraph()
        pulseSeries = BarGraphSeries(dataPoints)
        graph.addSeries(pulseSeries)
    }

    private fun configGraph() {
        //y-Achse
        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMinY(60.0)
        graph.viewport.setMaxY(150.0)

        //x-Achse
        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX((last.time - 60*1000).toDouble())
        graph.viewport.setMaxX((last.time).toDouble())

        //enable scrolling and scaling
        graph.viewport.isScalable = true
        graph.viewport.setScalableY(true)

        //Datumformat
        val df = SimpleDateFormat("E, HH:mm:ss")
        graph.gridLabelRenderer.labelFormatter = DateAsXAxisLabelFormatter(this, df)
        graph.gridLabelRenderer.numHorizontalLabels = 2
        graph.gridLabelRenderer.numVerticalLabels = 5
        graph.gridLabelRenderer.padding = 50
        graph.gridLabelRenderer.setHumanRounding(false)
        graph.title = getString(R.string.graph_title)
    }

    private fun getDataFromFile() {
        var fileInput : FileInputStream ? = null
        try {
            fileInput = openFileInput(DATA_FILENAME)
            val bufferdIn = BufferedReader(InputStreamReader(fileInput))
            var data = bufferdIn.readLine()
            while(data != null) {
                dataArray.add(data)
                data = bufferdIn.readLine()
            }
            bufferdIn.close()
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

    private fun parseJSONToSeries() {

        for(i in 0 until dataArray.size) {
            try {
                val jsonString = dataArray.get(i)
                val obj = JSONObject(jsonString)
                val pulse : Double = obj.getInt("puls").toDouble()

                val time = obj.getJSONArray("time")
                val year = time.getInt(0)
                val month = time.getInt(1)
                val day = time.getInt(2)
                val hour = time.getInt(3)
                val minute = time.getInt(4)
                val second = time.getInt(5)

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.set(Calendar.SECOND, second)

                last = calendar.time
                dataPoints[i] =  DataPoint(last, pulse)
            }
            catch (e : JSONException) {
                e.printStackTrace()
                dialogError(e.localizedMessage)
            }
        }
        Arrays.sort(dataPoints, object : Comparator<DataPoint?> {
            override fun compare(p0: DataPoint?, p1: DataPoint?): Int {
                if (p1!!.x > p0!!.x) return -1
                return if (p1.x < p0.x) 1 else 0
            }
        })
    }

    private fun dialogError(message : String) {
        alertDialog(title = getString(R.string.error_msg_title), message = message) {
            okButton{
                finish()
            }
        }.show()
    }
}