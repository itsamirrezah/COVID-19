package com.itsamirrezah.covid19.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.AreaCasesModel


/**
 * A simple [Fragment] subclass.
 */
class AreaDetailFragment : BottomSheetDialogFragment() {

    private lateinit var areaCaseModel: AreaCasesModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.TransparentBottomSheetDialogTheme)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_area_detail, container, false)
    }

}
