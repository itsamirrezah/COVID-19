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
import androidx.drawerlayout.widget.DrawerLayout
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
import com.itsamirrezah.covid19.data.novelapi.NovelApiImp
import com.itsamirrezah.covid19.ui.model.AreaModel
import com.itsamirrezah.covid19.util.SharedPreferencesUtil
import com.itsamirrezah.covid19.util.TransitionUtils
import com.itsamirrezah.covid19.util.Utils
import com.itsamirrezah.covid19.util.drawer.DrawerItem
import com.itsamirrezah.covid19.util.drawer.DrawerSearchItem
import com.itsamirrezah.covid19.util.drawer.SliderSearch
import com.itsamirrezah.covid19.util.map.AreaMarker
import com.itsamirrezah.covid19.util.map.ClusterItemInfoWindow
import com.mikepenz.materialdrawer.holder.ImageHolder
import com.mikepenz.materialdrawer.holder.StringHolder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.util.addStickyDrawerItems
import com.mikepenz.materialdrawer.widget.MaterialDrawerSliderView
import io.noties.markwon.Markwon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private val scope = CoroutineScope(Dispatchers.Main)
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var progressFab: ProgressBar
    private lateinit var progressSlider: ProgressBar
    private lateinit var fab: ExtendedFloatingActionButton
    private lateinit var slider: MaterialDrawerSliderView
    private lateinit var aboutBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mMap: GoogleMap
    private lateinit var mClusterManager: ClusterManager<AreaModel>

    //    private val compositeDisposable = CompositeDisposable()
    private lateinit var world: AreaModel
    private lateinit var areas: List<AreaModel>
    private lateinit var preferences: SharedPreferencesUtil

    private val sliderSearch = object : SliderSearch {

        override fun perform(searchEntry: CharSequence) {
            val filteredItems = areas.filter {
                return@filter it.country.toLowerCase().contains(searchEntry) ||
                        it.province.toLowerCase().contains(searchEntry.toString())
            }
            updateSliderItems(filteredItems)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        //init map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setupPreferences()
        setupProgressBars()
        setupFab()
        setupSliderView()
        setupAboutBottomSheet()
    }

    private fun setupPreferences() {
        preferences = SharedPreferencesUtil.getInstance(applicationContext)
    }

    private fun setupProgressBars() {
        progressFab = findViewById(R.id.progressWorld)
        progressSlider = findViewById(R.id.progressSlider)
        progressFab.visibility = View.VISIBLE
        progressSlider.visibility = View.VISIBLE
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
        aboutBottomSheetBehavior.skipCollapsed = true

        //fill about sheet text views
        val markWon = Markwon.create(applicationContext)
        fillAboutTextViews(
            markWon,
            findViewById(R.id.tvAppInfo),
            getString(R.string.app_info_title)
        )

        findViewById<TextView>(R.id.tvAppInfoVersion).append(
            packageManager.getPackageInfo(packageName, 0).versionName
        )

        fillAboutTextViews(
            markWon,
            findViewById(R.id.tvApis),
            getString(R.string.app_info_api)
        )

        fillAboutTextViews(
            markWon,
            findViewById(R.id.tvDeveloper),
            getString(R.string.app_info_developer)
        )
        fillAboutTextViews(
            markWon,
            findViewById(R.id.tvLibraries),
            getString(R.string.app_info_libraries)
        )
        fillAboutTextViews(
            markWon,
            findViewById(R.id.tvLicense),
            getString(R.string.app_info_licence)
        )
    }

    private fun fillAboutTextViews(markWon: Markwon, tv: TextView, text: String) {
        markWon.setMarkdown(tv, text)
    }

    private fun setupSliderView() {
        slider = findViewById(R.id.sliderView)
        drawerLayout = findViewById(R.id.root_drawer)
        //slider items:
        val searchItem = DrawerSearchItem(sliderSearch)
        val appInfoItem = PrimaryDrawerItem().apply {
            name = StringHolder("Application Info")
            textColor =
                ColorStateList.valueOf(Utils.getColor(applicationContext, R.color.grey_200))
            icon = ImageHolder(R.drawable.ic_github)
            isSelectable = false
        }

        slider.apply {
            addStickyDrawerItems(searchItem)
            addStickyDrawerItems(appInfoItem)
        }
        //setup slider item click listener
        slider.onDrawerItemClickListener = { _v, drawerItem, _position ->
            if (drawerItem is DrawerItem) {
                //hide soft keyboard
                Utils.hideKeyboard(this)
                //hide about bottomsheet if its open
                if (aboutBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN)
                    aboutBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                //relocate camera to selected area
                animateMapCamera(drawerItem.areaCasesModel.latLng!!, 5f)
            } else if (drawerItem is PrimaryDrawerItem) //app info screen
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
        getAllAreas()
        //todo: relocate camera to user's location
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(32.0, 53.0), 5f))
    }

    private fun setupClusterManager() {
        mClusterManager = ClusterManager(this, mMap)
        mClusterManager.algorithm.maxDistanceBetweenClusteredItems = 80
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
        mMap.uiSettings.isRotateGesturesEnabled = false

        //set dark style to map
        val mapStyleOption =
            MapStyleOptions.loadRawResourceStyle(
                this,
                R.raw.mapstyle_dark
            )
        mMap.setMapStyle(mapStyleOption)
        mMap.setInfoWindowAdapter(
            ClusterItemInfoWindow(applicationContext)
        )
        mMap.setOnInfoWindowClickListener {
            showAreaDetailFragment(it!!.tag as AreaModel)
        }

    }

    private fun getAllAreas() {
        scope.launch {
            NovelApiImp.getApi().getAllCases()
                //just get the locations that has at least one confirm case
                .filter { it.confirmedCases > 0 }
                //map data model to ui model
                .map {
                    AreaModel(
                        it.countryInfo.id,
                        LatLng(it.countryInfo.lat.toDouble(), it.countryInfo.lon.toDouble()),
                        it.countryName,
                        "",
                        it.confirmedCases,
                        it.deaths,
                        it.recovered
                    )
                }.toList()
                .also {
                    areas = it
                    updateClusterItems(it)
                    updateSliderItems(areas)
                    getWorld()
                }
        }
//        val requestAreas = NovelApiImp.getApi()
//            .getAllCases()
//            //returning locations one by one
//            .flatMap { Observable.fromIterable(it) }
//            //just get the locations that has at least one confirm case
//            .filter { it.confirmedCases > 0 }
//            //map data model to ui model
//            .map {
//                AreaModel(
//                    it.countryInfo.id,
//                    LatLng(it.countryInfo.lat.toDouble(), it.countryInfo.lon.toDouble()),
//                    it.countryName,
//                    "",
//                    it.confirmedCases,
//                    it.deaths,
//                    it.recovered
//                )
//            }
//            .toList()
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                areas = it
//                updateClusterItems(it)
//                updateSliderItems(areas)
//                getWorld()
//            }, {
//                print(it.message)
//            })
//
//        compositeDisposable.add(requestAreas)
    }

    private fun updateClusterItems(areas: List<AreaModel>) {
        mClusterManager.clearItems()
        for (item in areas) {
            mClusterManager.addItem(item)
        }
        mClusterManager.cluster()

    }

    //update slider items
    private fun updateSliderItems(areas: List<AreaModel>) {
        slider.itemAdapter.clear()
        val sortedAreas = areas.toMutableList()
        sortedAreas.sortByDescending { area -> area.confirmed }
        for (area in sortedAreas)
            slider.itemAdapter.add(DrawerItem(area))
        hideProgressSlider()

        if (preferences.isFirstRun) {
            drawerLayout.openDrawer(slider)
            preferences.isFirstRun = false
        }
    }

    private fun getWorld() {

        scope.launch {
            val response = NovelApiImp.getApi().getWorldCases()
            world = AreaModel(
                -1,
                response.confirmedCases,
                response.deaths,
                response.recovered,
                "Worldwide"
            )
            showWorldFab()
        }

//        val requestWorld = NovelApiImp.getApi()
//            .getWorldCases()
//            .map {
//                AreaModel(
//                    -1,
//                    it.confirmedCases,
//                    it.deaths,
//                    it.recovered,
//                    "Worldwide"
//                )
//            }
//            .subscribeOn(Schedulers.io())
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribe({
//                world = it
//                showWorldFab()
//            }, {
//                print(it.message)
//            })
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
            progressFab, 300,
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    progressFab.visibility = View.GONE
                }
            }
        )
    }

    private fun hideProgressSlider() {
        TransitionUtils.hideView(
            progressSlider, 300,
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    progressSlider.visibility = View.GONE
                }
            }
        )
    }

    private fun showAreaDetailFragment(areaCaseModel: AreaModel) {
        val bottomSheet = AreaDetailFragment()
        val bundle = Bundle()
        bundle.putParcelable("AREA_CASE_MODEL_EXTRA", areaCaseModel)
        bottomSheet.arguments = bundle
        bottomSheet.show(supportFragmentManager, bottomSheet.tag)
    }


    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(slider)) {
            drawerLayout.closeDrawers()
            return
        }
        if (aboutBottomSheetBehavior.state != BottomSheetBehavior.STATE_HIDDEN) {
            aboutBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            return
        }
        super.onBackPressed()
    }

}
