package com.itsamirrezah.covid19.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.pow


class AreaMarker(
    private val context: Context,
    mMap: GoogleMap,
    clusterManager: ClusterManager<AreaCasesModel>
) : DefaultClusterRenderer<AreaCasesModel>(context, mMap, clusterManager) {

    private var markerRootView: View =
        LayoutInflater.from(context).inflate(R.layout.area_case_marker, null) as LinearLayout


    private var clusterRootView =
        LayoutInflater.from(context).inflate(R.layout.cluster_marker, null) as FrameLayout

    private var clusterIconGen: IconGenerator = IconGenerator(context)

    private var markerIconGen: IconGenerator = IconGenerator(context)


    init {
        val clusterDrawable = ContextCompat.getDrawable(context, android.R.color.transparent)
        clusterIconGen.setBackground(clusterDrawable)
        clusterIconGen.setContentView(clusterRootView)

        val markerDrawable = ContextCompat.getDrawable(context, android.R.color.transparent)
        markerIconGen.setBackground(markerDrawable)
        markerIconGen.setContentView(markerRootView)
    }

    override fun onBeforeClusterItemRendered(item: AreaCasesModel?, markerOptions: MarkerOptions?) {
        markerRootView.findViewById<TextView>(R.id.tvCaseMarker).text =
            item!!.latestConfirmed.toString()
        markerRootView.findViewById<TextView>(R.id.tvDeathMarker).text =
            item.latestDeaths.toString()

        val icon = markerIconGen.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<AreaCasesModel>?,
        markerOptions: MarkerOptions?
    ) {

        val tvClusterCases = clusterRootView.findViewById<TextView>(R.id.tvClusterCases)
        val clusterCasesCount = cluster!!.items.sumBy { it.latestConfirmed.toInt() }

        tvClusterCases.text = when {
            clusterCasesCount.length() < 2 -> "1+"
            (10.0.pow(clusterCasesCount.length().toDouble()) / 2) > clusterCasesCount -> "${10.0.pow(
                clusterCasesCount.length() - 1
            ).toInt()}+"
            else -> "${(10.0.pow(clusterCasesCount.length()) / 2).toInt()}+"
        }

        val icon = clusterIconGen.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    companion object {
        fun Int.length() = when (this) {
            0 -> 1
            else -> log10(abs(toDouble())).toInt() + 1
        }
    }
}