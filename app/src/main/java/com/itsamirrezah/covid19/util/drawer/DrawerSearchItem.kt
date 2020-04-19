package com.itsamirrezah.covid19.util.drawer

import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.itsamirrezah.covid19.R
import com.mikepenz.materialdrawer.model.AbstractDrawerItem

class DrawerSearchItem(
    val sliderSearch: SliderSearch
) : AbstractDrawerItem<DrawerSearchItem, DrawerSearchItem.ViewHolder>() {

    val handler = Handler(Looper.getMainLooper())
    var searchRunnable: Runnable? = null

    override val layoutRes: Int
        get() = R.layout.drawer_search_item
    override val type: Int
        get() = R.id.DrawerSearchItem

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)

        holder.etSearchBar.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(textEntry: Editable?) {
                if (searchRunnable != null)
                    handler.removeCallbacks(searchRunnable!!)
                searchRunnable = Runnable {
                    sliderSearch.perform(textEntry.toString().toLowerCase().trim())
                }
                handler.postDelayed(searchRunnable!!, 500)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        holder.etSearchBar.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH){
                handler.removeCallbacks(searchRunnable!!)
                sliderSearch.perform(v.text.toString().toLowerCase().trim())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val etSearchBar: EditText = view.findViewById(R.id.etSearchBar)
    }
}

interface SliderSearch {
    fun perform(searchEntry: CharSequence)
}