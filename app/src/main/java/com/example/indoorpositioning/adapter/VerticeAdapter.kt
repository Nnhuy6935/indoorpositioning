package com.example.indoorpositioning.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.R

class VerticeAdapter : BaseAdapter() {
    var listData : MutableList<Point2D> = mutableListOf()
    fun setData(data : MutableList<Point2D>){
        listData = data
    }
    override fun getCount(): Int {
        return listData.size
    }

    override fun getItem(position: Int): Any {
        return listData[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        var dataItem = listData[position]
        var resultView : View
        var txtXValue : TextView
        var txtYValue : TextView
        var txtObjectId: TextView
        var layoutInflater : LayoutInflater = LayoutInflater.from(parent?.context)
        resultView = layoutInflater.inflate(R.layout.row_item, parent, false )
        txtXValue = resultView.findViewById(R.id.txtXValue)
        txtYValue = resultView.findViewById(R.id.txtYValue)
        txtObjectId = resultView.findViewById(R.id.txtObjectId)

        txtXValue.text = dataItem.getX().toString()
        txtYValue.text = dataItem.getY().toString()
        txtObjectId.text = dataItem.getId()
        return resultView
    }

}