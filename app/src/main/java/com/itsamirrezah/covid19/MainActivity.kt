package com.itsamirrezah.covid19

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
import com.itsamirrezah.covid19.data.api.CovidApiImp
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.itsamirrezah.covid19.util.AreaMarker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<AreaCasesModel>
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //init map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMap(mMap)
        setupClusterManager(mMap)
        getAllCases()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.0, 53.0), 5f))
    }

    private fun setupClusterManager(mMap: GoogleMap) {
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.algorithm.maxDistanceBetweenClusteredItems = 155
        mClusterManager.renderer = AreaMarker(this, mMap, mClusterManager)
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)

        mClusterManager.setOnClusterClickListener {
            val areas = mutableListOf<AreaCasesModel>()
            areas.addAll(it.items)
            val boundBuilder = LatLngBounds.builder()
            var clusterItemsCount = 0
            for (item in areas) {
                boundBuilder.include(item.position)
                clusterItemsCount++
            }
            if (clusterItemsCount > 1)
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 100))

            true
        }
    }

    private fun setupMap(googleMap: GoogleMap) {
        mMap.uiSettings.isRotateGesturesEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = true

        //set dark style to map
        val mapStyleOption =
            MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_shades_of_gray)
        mMap.setMapStyle(mapStyleOption)
    }

    private fun getAllCases() {
        val requestAllCases = CovidApiImp.getApi()
            .getAllCases()
            //map data model to ui model
            .map {
                val areaCasesModel = mutableListOf<AreaCasesModel>()
                for (area in it.confirmed.locations) {
                    val index = it.confirmed.locations.indexOf(area)
                    //just get the locations that has at least one confirm case
                    if (it.confirmed.locations[index].latest < 1)
                        continue

                    areaCasesModel.add(
                        AreaCasesModel(
                            area.coordinates.lat,
                            area.coordinates.long,
                            area.country,
                            area.countryCode,
                            area.province,
                            it.deaths.locations[index].history,
                            area.history,
                            it.recovered.locations[index].history,
                            it.confirmed.locations[index].latest,
                            it.deaths.locations[index].latest,
                            it.recovered.locations[index].latest
                        )
                    )
                }
                areaCasesModel
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mClusterManager.clearItems()
                mClusterManager.addItems(it)
                mClusterManager.cluster()

            }, {
                print(it.message)
            })

        compositeDisposable.add(requestAllCases)
    }
}
