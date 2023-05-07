package com.example.vehiclesharingsystemandroidapplication.view.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.vehiclesharingsystemandroidapplication.R
import com.example.vehiclesharingsystemandroidapplication.model.RentalSubscription
import com.example.vehiclesharingsystemandroidapplication.view.ui.selectSubscription.RentalSubscriptionViewModel

class ListAdapter (private val c: Context, private var dataSource: ArrayList<RentalSubscriptionViewModel>) : BaseAdapter() {

    private val inflater : LayoutInflater = c.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.count()
    }

    override fun getItem(p0: Int): RentalSubscriptionViewModel {
        return dataSource[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, containerView: View?, viewGroupParent: ViewGroup?): View {

        val rowView = containerView ?: inflater.inflate(R.layout.subscription_list_item,viewGroupParent,false)

        val name = rowView.findViewById<TextView>(R.id.subscription_name_textbox)
        name.text = getItem(position).rentalSubscription.name

        val kilometersLimit = rowView.findViewById<TextView>(R.id.subscription_km_limit_textbox)
        kilometersLimit.text = getItem(position).rentalSubscription.kilometerLimit.toString()

        val price = rowView.findViewById<TextView>(R.id.subscription_price_textbox)
        price.text = getItem(position).rentalSubscription.rentalPrice.toString()

        if(getItem(position).selected){
            rowView.setBackgroundResource(R.drawable.layout_border)
        }else{
            rowView.setBackgroundResource(0)
        }
        return rowView
    }

}