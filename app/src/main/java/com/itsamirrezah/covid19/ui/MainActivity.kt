package com.itsamirrezah.covid19.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.maps.android.clustering.ClusterManager
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.data.api.CovidApiImp
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.itsamirrezah.covid19.util.TransitionUtils
import com.itsamirrezah.covid19.util.Utils
import com.itsamirrezah.covid19.util.drawer.DrawerItem
import com.itsamirrezah.covid19.util.map.AreaMarker
import com.itsamirrezah.covid19.util.map.ClusterItemInfoWindow
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.util.addStickyDrawerItems
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import io.noties.markwon.Markwon
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.LocalDate

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var aboutBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<AreaCasesModel>
    private val compositeDisposable = CompositeDisposable()
    private lateinit var world: AreaCasesModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //init map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupFab()
        setupSliderView()
        setupAboutBottomSheet()
    }

    private fun setupFab() {
        fab = findViewById(R.id.fab)
        fab.shrink()

        fab.setOnClickListener {
            showAreaDetailFragment(world)
        }
        fab.setOnLongClickListener {
            if (!fab.isExtended) {
                fab.extend()
                fabOnChangedCallback.onExtended(fab)
                return@setOnLongClickListener true
            }
            return@setOnLongClickListener false
        }
    }

    private val fabOnChangedCallback = object : ExtendedFloatingActionButton.OnChangedCallback() {
        override fun onExtended(extendedFab: ExtendedFloatingActionButton?) {
            Handler().postDelayed({
                extendedFab!!.shrink()
            }, 2000)
        }
    }

    private fun setupAboutBottomSheet() {
        val aboutBottomSheetView = findViewById<View>(R.id.about_bottomsheet_root)
        aboutBottomSheetBehavior = BottomSheetBehavior.from(aboutBottomSheetView)
        aboutBottomSheetBehavior.isHideable = true
        aboutBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        val markWon = Markwon.create(applicationContext)
        fillAboutTextViews(markWon, findViewById(R.id.tvAppInfo), getString(R.string.app_info_title))
        fillAboutTextViews(markWon, findViewById(R.id.tvAppInfoDescription), getString(R.string.app_info_description))
        fillAboutTextViews(markWon, findViewById(R.id.tvDeveloper), getString(R.string.app_info_developer))
        fillAboutTextViews(markWon, findViewById(R.id.tvLibraries), getString(R.string.app_info_libraries))
        fillAboutTextViews(markWon, findViewById(R.id.tvLicense), getString(R.string.app_info_licence))
    }

    private fun fillAboutTextViews(markWon: Markwon, tv: TextView, text: String) {
        markWon.setMarkdown(tv, text)
    }

    private fun setupSliderView() {
        slider = findViewById(R.id.sliderView)
        //header item on top
        val headerItem = PrimaryDrawerItem().apply {
            name = StringHolder("Countries")
            textColor =
                ColorStateList.valueOf(ContextCompat.getColor(applicationContext, R.color.grey_200))
            isSelectable = false
        }
        //sticky item on bottom
        val stickyItem = SecondaryDrawerItem().apply {
            name = StringHolder("Application Info")
            textColor =
                ColorStateList.valueOf(ContextCompat.getColor(application, R.color.grey_200))
            icon = ImageHolder(R.drawable.ic_github)
            isSelectable = false
        }

        slider.apply {
            itemAdapter.add(headerItem)
            addStickyDrawerItems(stickyItem)
        }
        //setup slider item click listener
        slider.onDrawerItemClickListener = { _v, drawerItem, _position ->
            if (drawerItem is DrawerItem) {
                //relocate camera to selected area
                animateMapCamera(drawerItem.areaCasesModel.latLng!!, 5f)
            } else if (drawerItem is SecondaryDrawerItem) //app info screen
                aboutBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            false
        }
    }

    private fun animateMapCamera(latLng: LatLng, zoom: Float) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

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
            showAreaDetailFragment(it!!.tag as AreaCasesModel)
        }

    }

    private fun getAllCases() {
        val requestAllCases = CovidApiImp.getApi()
            .getAllCases(false)
            //returning locations one by one
            .flatMap { Observable.fromIterable(it.areas) }
            //just get the locations that has at least one confirm case
            .filter { it.latest.confirmed > 0 }
            //map data model to ui model
            .map {
                AreaCasesModel(
                    it.id,
                    LatLng(it.coordinates.lat.toDouble(), it.coordinates.lon.toDouble()),
                    it.country,
                    it.countryCode,
                    it.province,
                    it.latest.confirmed,
                    it.latest.deaths,
                    it.latest.recovered
                )
            }
            .toList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                mClusterManager.clearItems()
                mClusterManager.addItems(it)
                mClusterManager.cluster()
                updateSliderItems(it)
                makeWorld()
            }, {
                print(it.message)
            })

        compositeDisposable.add(requestAllCases)
    }

    //update slider items
    private fun updateSliderItems(areas: MutableList<AreaCasesModel>) {
        areas.sortByDescending { area -> area.confirmed }
        for (area in areas)
            slider.itemAdapter.add(DrawerItem(area))
    }

    private fun makeWorld() {
        val requestWorld = CovidApiImp.getApi()
            .getAllCases(true)
            //just get the locations that has at least one confirm case
            .filter { it.latest.confirmed > 0 }
            .map {
                //setup world timeline
                val timelines: MutableList<Pair<LocalDate, Triple<Int, Int, Int>>> = mutableListOf()
                val dailyTimeline: MutableList<Pair<LocalDate, Triple<Int, Int, Int>>> =
                    mutableListOf()

                for (area in it.areas) {
                    val confirmedTimeline = area.timelines.confirmed.timeline
                    val deathsTimeline = area.timelines.deaths.timeline
                    val recoveredTimeline = area.timelines.recovered.timeline

                    //pair = Pair(key,value)
                    for ((index, pair) in confirmedTimeline.toList().withIndex()) {
                        val confirmed = pair.second +
                                timelines.getOrElse(index) {
                                    Pair(
                                        null,
                                        Triple(0, 0, 0)
                                    )
                                }.second.first
                        val deaths = (deathsTimeline[pair.first] ?: 0) +
                                timelines.getOrElse(index) {
                                    Pair(
                                        null,
                                        Triple(0, 0, 0)
                                    )
                                }.second.second
                        val recovered = (recoveredTimeline[pair.first] ?: 0) +
                                timelines.getOrElse(index) {
                                    Pair(
                                        null,
                                        Triple(0, 0, 0)
                                    )
                                }.second.third
                        val localDate = Utils.toLocalDate(pair.first)

                        val data = Pair(localDate, Triple(confirmed, deaths, recovered))
                        if (timelines.size <= index)
                            timelines.add(data)
                        else
                            timelines[index] = data
                    }
                }

                //setup world daily
                for ((index, timeline) in timelines.withIndex()) {
                    val confirmed = timeline.second.first
                    val deaths = timeline.second.second
                    val recovered = timeline.second.third
                    val dailyConfirmed =
                        confirmed - timelines.getOrElse(index - 1) {
                            Pair(null, Triple(0, 0, 0))
                        }.second.first
                    val dailyDeaths = deaths - timelines.getOrElse(index - 1) {
                        Pair(null, Triple(0, 0, 0))
                    }.second.second
                    val dailyRecovered = recovered - timelines.getOrElse(index - 1) {
                        Pair(null, Triple(0, 0, 0))
                    }.second.third

                    dailyTimeline.add(
                        Pair(
                            timeline.first,
                            Triple(
                                dailyConfirmed, dailyDeaths, dailyRecovered
                            )
                        )
                    )
                }
                world = AreaCasesModel(
                    it.latest.confirmed,
                    it.latest.deaths,
                    it.latest.recovered,
                    timelines,
                    dailyTimeline
                )
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                showWorldFab()
            }, {
                print(it.message)
            })

        compositeDisposable.add(requestWorld)
    }

    private fun showWorldFab() {
        TransitionUtils.revealView(
            fab,
            300,
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    fab.extend(fabOnChangedCallback)
                }
            }
        )

        TransitionUtils.hideView(
            findViewById<ProgressBar>(R.id.progressWorld),
            300,
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    findViewById<ProgressBar>(R.id.progressWorld).visibility = View.GONE
                }
            }
        )
    }

    private fun showAreaDetailFragment(areaCaseModel: AreaCasesModel) {
        val bottomSheet = AreaDetailFragment()
        val bundle = Bundle()
        bundle.putParcelable("AREA_CASE_MODEL_EXTRA", areaCaseModel)
        bottomSheet.arguments = bundle
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }

}
