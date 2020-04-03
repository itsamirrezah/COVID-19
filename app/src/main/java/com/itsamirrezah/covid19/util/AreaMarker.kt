package com.itsamirrezah.covid19.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.itsamirrezah.covid19.util.Utils.Companion.length


class AreaMarker(
    private val context: Context,
    private val mMap: GoogleMap,
    clusterManager: ClusterManager<AreaCasesModel>
) : DefaultClusterRenderer<AreaCasesModel>(context, mMap, clusterManager),
    ClusterManager.OnClusterClickListener<AreaCasesModel> {

    private var markerRootView: View =
        LayoutInflater.from(context).inflate(R.layout.area_case_marker, null) as LinearLayout

    private var clusterRootView =
        LayoutInflater.from(context).inflate(R.layout.cluster_marker, null) as FrameLayout

    private var clusterIconGen: IconGenerator = IconGenerator(context)

    private var markerIconGen: IconGenerator = IconGenerator(context)

    init {
        clusterManager.setOnClusterClickListener(this)
        val clusterDrawable = ContextCompat.getDrawable(context, android.R.color.transparent)
        clusterIconGen.setBackground(clusterDrawable)
        clusterIconGen.setContentView(clusterRootView)

        val markerDrawable = ContextCompat.getDrawable(context, android.R.color.transparent)
        markerIconGen.setBackground(markerDrawable)
        markerIconGen.setContentView(markerRootView)
    }

    override fun onBeforeClusterItemRendered(item: AreaCasesModel?, markerOptions: MarkerOptions?) {
        markerRootView.findViewById<TextView>(R.id.tvCaseMarker).text =
            item!!.latestConfirmed
        markerRootView.findViewById<TextView>(R.id.tvDeathMarker).text =
            item.latestDeaths

        val icon = markerIconGen.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<AreaCasesModel>?,
        markerOptions: MarkerOptions?
    ) {
        val tvClusterCases = clusterRootView.findViewById<TextView>(R.id.tvClusterCases)

        val clusterCasesCount = cluster!!.items.sumBy { it.latestConfirmed.toInt() }
        tvClusterCases.text = "+".plus(Utils.randDigit(clusterCasesCount))
        clusterRootView.backgroundTintList =
            ContextCompat.getColorStateList(context, getClusterColor(clusterCasesCount))

        val icon = clusterIconGen.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<AreaCasesModel>?): Boolean {
        if (cluster!!.size>1)
            return true
        return false
    }


    override fun onClusterClick(cluster: Cluster<AreaCasesModel>?): Boolean {
        val areas = mutableListOf<AreaCasesModel>()
        areas.addAll(cluster!!.items)
        val boundBuilder = LatLngBounds.builder()
        var clusterItemsCount = 0
        for (item in areas) {
            boundBuilder.include(item.position)
            clusterItemsCount++
        }
        if (clusterItemsCount > 1)
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 100))

        return true
    }

    private fun getClusterColor(clusterCases: Int): Int {
        return when (clusterCases.length()) {
            1 -> R.color.yellow_700
            2 -> R.color.amber_800
            3 -> R.color.yellow_900
            4 -> R.color.deep_orange_900
            else -> R.color.red_900
        }
    }
    //todo:change marker & cluster layout
}