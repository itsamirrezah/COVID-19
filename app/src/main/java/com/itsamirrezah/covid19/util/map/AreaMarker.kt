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
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.itsamirrezah.covid19.util.Utils
import kotlin.math.min


class AreaMarker(
    private val context: Context,
    private val mMap: GoogleMap,
    clusterManager: ClusterManager<AreaCasesModel>
) : DefaultClusterRenderer<AreaCasesModel>(context, mMap, clusterManager),
    ClusterManager.OnClusterClickListener<AreaCasesModel> {

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

    override fun onClusterItemRendered(clusterItem: AreaCasesModel?, marker: Marker?) {
        marker!!.tag = clusterItem
    }

    override fun onBeforeClusterItemRendered(item: AreaCasesModel?, markerOptions: MarkerOptions?) {
        tvCaseMarker.text = item!!.latestConfirmed
        val icon = markerIconGen.makeIcon()
        markerOptions!!.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onBeforeClusterRendered(
        cluster: Cluster<AreaCasesModel>?,
        markerOptions: MarkerOptions?
    ) {
        val clusterCasesCount = cluster!!.items.sumBy { it.latestConfirmed.toInt() }
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

    override fun shouldRenderAsCluster(cluster: Cluster<AreaCasesModel>?): Boolean {
        if (cluster!!.size > 1)
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
        if (clusterItemsCount > 3)
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 100))

        return true
    }

    private fun getClusterOverlayColor(clusterCases: Int): Int {
        val ratio = min(clusterCases / 40000f, 1f)
        return Utils.blendColors(
            context,
            R.color.overlay_light_30,
            R.color.overlay_dark_40,
            ratio
        )
    }
}