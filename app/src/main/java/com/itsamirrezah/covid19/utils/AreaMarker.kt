package com.itsamirrezah.covid19.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import kotlin.math.roundToInt

class AreaMarker(
    private val context: Context,
    mMap: GoogleMap,
    clusterManager: ClusterManager<AreaCasesModel>
) : DefaultClusterRenderer<AreaCasesModel>(context, mMap, clusterManager) {

    override fun onBeforeClusterItemRendered(item: AreaCasesModel?, markerOptions: MarkerOptions?) {
        var view = (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.area_case_marker, null) as LinearLayout
        val latestCases = item!!.latestConfirmed
        val latestDeaths = item.latestDeaths

        view.findViewById<TextView>(R.id.tvCaseMarker).text = latestCases.toString()
        view.findViewById<TextView>(R.id.tvDeathMarker).text = latestDeaths.toString()

        markerOptions!!.icon(getBitmapFromView(view, 60f, 105f))
    }

    companion object {

        fun getBitmapFromView(view: View, width: Float, height: Float): BitmapDescriptor? {

            view.measure(
                View.MeasureSpec.makeMeasureSpec(
                    dpPxConverter(width),
                    View.MeasureSpec.EXACTLY
                ),
                View.MeasureSpec.makeMeasureSpec(
                    dpPxConverter(height),
                    View.MeasureSpec.EXACTLY
                )
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)

            val bitmap =
                Bitmap.createBitmap(
                    view.measuredWidth,
                    view.measuredHeight,
                    Bitmap.Config.ARGB_8888
                )
            val canvas = Canvas(bitmap)
            view.draw(canvas)
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }


        private fun dpPxConverter(dp: Float): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                Resources.getSystem().displayMetrics
            ).roundToInt()
        }
    }
}