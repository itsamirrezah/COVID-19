package com.itsamirrezah.covid19.util.drawer

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itsamirrezah.covid19.R
import com.itsamirrezah.covid19.ui.model.AreaCasesModel
import com.mikepenz.materialdrawer.model.AbstractDrawerItem

class DrawerItem(
    val areaCasesModel: AreaCasesModel
) : AbstractDrawerItem<DrawerItem, DrawerItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.drawer_item
    override val type: Int
        get() = R.id.DrawerItem

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override fun bindView(holder: ViewHolder, payloads: List<Any>) {
        super.bindView(holder, payloads)

        if (areaCasesModel.province != "") {
            holder.country.text = areaCasesModel.province
            holder.province.visibility = View.VISIBLE
            holder.province.text = areaCasesModel.country
        } else {
            holder.province.visibility = View.GONE
            holder.country.text = areaCasesModel.country
        }
        holder.value.text = areaCasesModel.confirmedString
    }

    override fun unbindView(holder: ViewHolder) {
        super.unbindView(holder)
        holder.country.text = ""
        holder.province.text = ""
        holder.value.text = ""
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val country: TextView = view.findViewById(R.id.tvCountry)
        val province: TextView = view.findViewById(R.id.tvProvince)
        val value: TextView = view.findViewById(R.id.tvConfirmed)
    }


}