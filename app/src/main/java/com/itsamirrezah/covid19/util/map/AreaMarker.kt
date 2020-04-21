package com.itsamirrezah.covid19.util.map

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.AreaModel
import com.itsamirrezah.covid19.util.Utils
import kotlin.math.min


class AreaMarker(
    private val context: Context,
    private val mMap: GoogleMap,
    clusterManager: ClusterManager<AreaModel>
) : DefaultClusterRenderer<AreaModel>(context, mMap, clusterManager),
    ClusterManager.OnClusterClickListener<AreaModel> {

    private var markerRootView: View =
        LayoutInflater.from(context).inflate(R.layout.cluster_marker, null) as FrameLayout

    private var clusterRootView =
        LayoutInflater.from(context).inflate(R.layout.cluster_marker, null) as FrameLayout

    private val tvCaseMarker: TextView = markerRootView.findViewById(R.id.amu_text)
        get() {
            field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            val fourDpi = 6 * context.resources.displayMetrics.density.toInt()
            field.setPadding(fourDpi, fourDpi, fourDpi, fourDpi)
            return field
        }

    private val tvCaseCluster: TextView = clusterRootView.findViewById(R.id.amu_text)
        get() {
            field.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            val sixDpi = 4 * context.resources.displayMetrics.density.toInt()
            field.setPadding(sixDpi, sixDpi, sixDpi, sixDpi)
            return field
        }


    private var clusterIconGen: IconGenerator = IconGenerator(context)

    private var markerIconGen: IconGenerator = IconGenerator(context)

    init {
        clusterManager.setOnClusterClickListener(this)
        markerRootView.alpha = 0.6f
        val transparentDrawable = ContextCompat.getDrawable(context, android.R.color.transparent)
        clusterIconGen.setBackground(transparentDrawable)
        clusterIconGen.setContentView(clusterRootView)
        markerIconGen.setBackground(transparentDrawable)
        markerIconGen.setContentView(markerRootView)
    }

    override fun onClusterItemRendered(clusterItem: AreaModel?, marker: Marker?) {
        marker!!.tag = clusterItem
    }

    override fun onBeforeClusterItemRendered(item: AreaModel?, markerOptions: MarkerOptions?) {
        tvCaseMarker.text = item!!.confirmedString
        val icon = markerIconGen.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<AreaModel>?,
        markerOptions: MarkerOptions?
    ) {
        val clusterCasesCount = cluster!!.items.sumBy { it.confirmed.toInt() }
        tvCaseCluster.text = "+".plus(
            Utils.randDigit(
                clusterCasesCount
            )
        )
        clusterRootView.findViewById<FrameLayout>(R.id.overlay).backgroundTintList =
            ColorStateList.valueOf(getClusterOverlayColor(clusterCasesCount))
        val icon = clusterIconGen.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun shouldRenderAsCluster(cluster: Cluster<AreaModel>?): Boolean {
        if (cluster!!.size > 2)
            return true
        return false
    }

    override fun onClusterClick(cluster: Cluster<AreaModel>?): Boolean {
        val areas = mutableListOf<AreaModel>()
        areas.addAll(cluster!!.items)
        val boundBuilder = LatLngBounds.builder()
        var clusterItemsCount = 0
        for (item in areas) {
            boundBuilder.include(item.position)
            clusterItemsCount++
        }
        if (clusterItemsCount > 2)
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 80))

        return true
    }

    private fun getClusterOverlayColor(clusterCases: Int): Int {
        val ratio = min(clusterCases / 90000f, 1f)
        return Utils.blendColors(
            context,
            R.color.overlay_light_10,
            R.color.overlay_dark_50,
            ratio
        )
    }
}