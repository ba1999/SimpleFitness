package com.example.simplefitness

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries

class GraphDataActivity : AppCompatActivity() {

    private val graph : GraphView by lazy { findViewById(R.id.graph) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_data)

        val series = LineGraphSeries<DataPoint>(arrayOf<DataPoint>(
            DataPoint(0.0, 1.0), DataPoint(1.0, 5.0), DataPoint(2.0, 3.0), DataPoint(3.0, 1.5), DataPoint(4.0, 6.0)
        ))
        graph.addSeries(series)

    }
}