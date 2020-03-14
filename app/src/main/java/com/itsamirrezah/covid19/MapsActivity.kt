package com.itsamirrezah.covid19

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.maps.android.clustering.ClusterManager
import com.itsamirrezah.covid19.data.api.CovidApiImp
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<AreaCasesModel>
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mClusterManager = ClusterManager<AreaCasesModel>(this, mMap)
        mMap.setOnCameraIdleListener(mClusterManager)
        getAllCases()
    }

    private fun getAllCases() {
        val requestAllCases = CovidApiImp.getApi()
            .getAllCases()
            .map {
                //map data model to ui model
                val areaCasesModel = mutableListOf<AreaCasesModel>()
                for (area in it.confirmed.locations) {
                    val index = it.confirmed.locations.indexOf(area)
                    areaCasesModel.add(
                        AreaCasesModel(
                            area.coordinates.lat,
                            area.coordinates.long,
                            area.country,
                            area.countryCode,
                            area.province,
                            it.deaths.locations[index].history,
                            area.history,
                            it.recovered.locations[index].history
                        )
                    )
                }
                areaCasesModel
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mClusterManager.clearItems()
                mClusterManager.cluster()
                mClusterManager.addItems(it)
                mClusterManager.cluster()

            }, {
                print(it.message)
            })

        compositeDisposable.add(requestAllCases)
    }
}
