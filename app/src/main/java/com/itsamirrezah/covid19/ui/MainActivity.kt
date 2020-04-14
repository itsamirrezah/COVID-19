package com.itsamirrezah.covid19.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterManager
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.data.api.CovidApiImp
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.itsamirrezah.covid19.util.map.AreaMarker
import com.itsamirrezah.covid19.util.map.ClusterItemInfoWindow
import io.reactivex.Observable
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
        setupMap()
        setupClusterManager()
        getAllCases()
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.0, 53.0), 5f))
    }

    private fun setupClusterManager() {
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.algorithm.maxDistanceBetweenClusteredItems = 100
        mClusterManager.renderer =
            AreaMarker(this, mMap, mClusterManager)
        mMap.setOnCameraIdleListener(mClusterManager)
        mMap.setOnMarkerClickListener(mClusterManager)
    }

    private fun setupMap() {
        mMap.uiSettings.isRotateGesturesEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false

        //set dark style to map
        val mapStyleOption =
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.mapstyle_dark
            )
        mMap.setMapStyle(mapStyleOption)
        mMap.setInfoWindowAdapter(
            ClusterItemInfoWindow(
                applicationContext
            )
        )
        mMap.setOnInfoWindowClickListener {
            val bottomSheet = AreaDetailFragment()
            val bundle = Bundle()
            bundle.putParcelable("AREA_CASE_MODEL_EXTRA", it!!.tag as AreaCasesModel)
            bottomSheet.arguments = bundle
            bottomSheet.show(supportFragmentManager, bottomSheet.tag)
        }

    }

    private fun getAllCases() {
        val requestAllCases = CovidApiImp.getApi()
            .getAllCases()
            //returning locations one by one
            .flatMap { Observable.fromIterable(it.areas) }
            //just get the locations that has at least one confirm case
            .filter { it.latest.confirmed > 0 }
            //map data model to ui model
            .map {
                AreaCasesModel(
                    it.id,
                    it.coordinates.lat.toDouble(),
                    it.coordinates.lon.toDouble(),
                    it.country,
                    it.countryCode,
                    it.province,
                    it.latest.confirmed.toString(),
                    it.latest.deaths.toString(),
                    it.latest.recovered.toString()
                )
            }
            .toList()
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
