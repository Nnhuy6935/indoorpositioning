package com.example.indoorpositioning.map

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.indoorpositioning.Interface.OnIntentToEditActivity
import com.example.indoorpositioning.Interface.OnUpdateRenderObject
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.R
import com.example.indoorpositioning.canvas.concave.ConcavePolygonActivity
import com.example.indoorpositioning.canvas.curve.CurveActivity
import com.example.indoorpositioning.canvas.mixObject.MixObjectActivity
import com.example.indoorpositioning.canvas.normal.AddObjectActivity

class MainActivity : AppCompatActivity(), View.OnClickListener, OnIntentToEditActivity {
    val INTENT_ADD_NEW_OBJECT = 131313
    val INTENT_EDIT_EXIST_OBJECT = 111333
    val INTENT_ADD_CONCAVE_POLYGON = 121212
    val INTENT_EDIT_CONCAVE_POLYGON = 111222
    val INTENT_ADD_CURVE = 101010
    val INTENT_EDIT_CURVE = 111000
    val INTENT_ADD_MIX_OBJECT = 141414
    val INTENT_EDIT_MIX_OBJECT = 111444

    lateinit var mapView : MyGLSurfaceView
    lateinit var btnAdd : Button
    lateinit var btnAddConcave : Button
    lateinit var btnAddCurve : Button
    lateinit var btnMix : Button
    lateinit var updateRendererCallback : OnUpdateRenderObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
        mapView = findViewById(R.id.mapview)
        mapView.setCallback(this)
        updateRendererCallback = mapView
        btnAdd = findViewById(R.id.btnAddObj)
        btnAdd.setOnClickListener(this)
        btnAddConcave = findViewById(R.id.btnAddConcave)
        btnAddConcave.setOnClickListener(this)
        btnAddCurve = findViewById(R.id.btnAddCurve)
        btnAddCurve.setOnClickListener(this)
        btnMix = findViewById(R.id.btnAddMix)
        btnMix.setOnClickListener(this)
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onClick(view: View?) {
        if(view?.id == R.id.btnAddObj){
            var intent = Intent(this, AddObjectActivity::class.java)
            startActivityForResult(intent, INTENT_ADD_NEW_OBJECT)
        }
        else if(view?.id == R.id.btnAddConcave){
            var intent  = Intent(this, ConcavePolygonActivity::class.java)
            startActivityForResult(intent, INTENT_ADD_CONCAVE_POLYGON)
        }
        else if(view?.id == R.id.btnAddCurve){
            var intent = Intent(this, CurveActivity::class.java)
            startActivityForResult(intent, INTENT_ADD_CURVE)
        }
        else if(view?.id == R.id.btnAddMix){
            var intent = Intent(this, MixObjectActivity::class.java)
            startActivityForResult(intent, INTENT_ADD_MIX_OBJECT)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == INTENT_ADD_NEW_OBJECT){
            if(resultCode == RESULT_OK){
                // get list Point data
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices : MutableList<Point2D> = mutableListOf()
                if (verticesData != null) {
                    for(point in verticesData){
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }
                if(vertices.size > 0) {
                    Toast.makeText(this,"Added",Toast.LENGTH_SHORT).show()
                    var objType = defineObjectType(vertices,false)
                    updateRendererCallback.onUpdate(UpdateType.ADD, vertices, groupId!!, objType, fillColor, borderColor)
                }else{
                    Toast.makeText(this,"Empty Point",Toast.LENGTH_SHORT).show()
                }
            }else{
            }
        }
        else if(requestCode == INTENT_EDIT_EXIST_OBJECT){
            if(resultCode == RESULT_OK){
                //get list Point data
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices : MutableList<Point2D> = mutableListOf()
                if (verticesData != null) {
                    for(point in verticesData){
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }
                var objType = defineObjectType(vertices,false)
                if(vertices?.size == 0){
                    Toast.makeText(this,"DELETE $objType",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.DELETE, vertices, groupId!!, objType, fillColor, borderColor)
                }else {
                    Toast.makeText(this,"EDIT $objType",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.EDIT, vertices, groupId!!, objType, fillColor, borderColor)
                }
            }else{
                //todo: nothing
            }
        }
        else if(requestCode == INTENT_ADD_CONCAVE_POLYGON){
            if(resultCode == RESULT_OK){
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices : MutableList<Point2D> = mutableListOf()
                if (verticesData != null) {
                    for(point in verticesData){
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }
                if(vertices.size > 0) {
                    Toast.makeText(this, "Added",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(
                        UpdateType.ADD,
                        vertices,
                        groupId!!,
                        defineObjectType(vertices, true),
                        fillColor,
                        borderColor
                    )
                }else{
                    Toast.makeText(this,"Empty Point",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
            }
        }
        else if(requestCode == INTENT_EDIT_CONCAVE_POLYGON){
            if(resultCode == RESULT_OK){
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices : MutableList<Point2D> = mutableListOf()
                if (verticesData != null) {
                    for(point in verticesData){
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }
                var objType = defineObjectType(vertices, true)
                if(vertices?.size == 0){
                    Toast.makeText(this,"Deleted $objType",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.DELETE, vertices, groupId!!,objType, fillColor, borderColor)
                }else {
                    Toast.makeText(this,"Edit $objType",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.EDIT, vertices, groupId!!, objType, fillColor, borderColor)
                }
            }else{
                //todo: nothing
            }
        }
        else if(requestCode == INTENT_ADD_CURVE){
            if(resultCode == RESULT_OK) {
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices: MutableList<Point2D> = mutableListOf()
                if (verticesData != null) {
                    for (point in verticesData) {
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }
                if (vertices.size > 0) {
                    Toast.makeText(this, "Added", Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(
                        UpdateType.ADD,
                        vertices,
                        groupId!!,
                        ObjectType.CURVE,
                        fillColor,
                        borderColor
                    )
                }
            }
        }
        else if(requestCode == INTENT_EDIT_CURVE){
            if(resultCode == RESULT_OK){
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices : MutableList<Point2D> = mutableListOf()
                if (verticesData != null) {
                    for(point in verticesData){
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }
                if(vertices?.size  == 0){
                    Toast.makeText(this,"DELETE CURVE",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.DELETE, vertices, groupId!!, ObjectType.CURVE, fillColor, borderColor)
                }else{
                    Toast.makeText(this,"EDIT CURVE",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.EDIT, vertices, groupId!!, ObjectType.CURVE, fillColor, borderColor)
                }
            }
        }
        else if(requestCode == INTENT_ADD_MIX_OBJECT){
            if(resultCode == RESULT_OK){
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices : MutableList<Point2D> = mutableListOf()
                if (verticesData != null) {
                    for(point in verticesData){
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }

                if(vertices.size > 0) {
                    Toast.makeText(this, "Added",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(
                        UpdateType.ADD,
                        vertices,
                        groupId!!,
                        ObjectType.MIX,
                        fillColor,
                        borderColor
                    )
                }else{
                    Toast.makeText(this,"Empty Point",Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show()
            }
        }
        else if(requestCode == INTENT_EDIT_MIX_OBJECT){
            if(resultCode == RESULT_OK){
                var verticesData = data?.extras?.getParcelableArrayList<Point2D>("data")
                var groupId = data?.extras?.getString("groupId")
                var fillColor = data?.extras?.getInt("fillColor")
                var borderColor = data?.extras?.getInt("borderColor")
                var vertices : MutableList<Point2D> = mutableListOf()
                if(verticesData != null){
                    for(point in verticesData){
                        point.setBoudingBox()
                        vertices.add(point)
                    }
                }
                if(vertices.size == 0){
                    Toast.makeText(this, "DELETE MIX OBJECT",Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.DELETE, vertices, groupId!!, ObjectType.MIX, fillColor, borderColor)
                }else{
                    Toast.makeText(this, "EDIT MIX OBJECT", Toast.LENGTH_SHORT).show()
                    updateRendererCallback.onUpdate(UpdateType.EDIT, vertices, groupId!!, ObjectType.MIX, fillColor, borderColor)
                }
            }
        }
    }
    override fun onStartIntentToEditActivity(
        vertices: MutableList<Point2D>,
        type: ObjectType,
        fillColor: Int,
        borderColor: Int
    ) {
        if(type == ObjectType.CONCAVE_POLYGON){
            var intent = Intent(this, ConcavePolygonActivity::class.java)
            var data : ArrayList<Point2D> = ArrayList(vertices)
            var args = Bundle()
            args.putParcelableArrayList("data",data)
            args.putInt("fillColor",fillColor)
            args.putInt("borderColor",borderColor)
            intent.putExtras(args)
            startActivityForResult(intent,INTENT_EDIT_CONCAVE_POLYGON)

        }
        else if(type == ObjectType.CURVE){
            var intent = Intent(this, CurveActivity::class.java)
            var data : ArrayList<Point2D> = ArrayList(vertices)
            var args = Bundle()
            args.putParcelableArrayList("data",data)
            args.putInt("fillColor",fillColor)
            args.putInt("borderColor",borderColor)
            intent.putExtras(args)
            startActivityForResult(intent, INTENT_EDIT_CURVE)
        }
        else if(type == ObjectType.MIX){
            var intent = Intent(this, MixObjectActivity::class.java)
            var data : ArrayList<Point2D> = ArrayList(vertices)
            var args = Bundle()
            args.putParcelableArrayList("data",data)
            args.putInt("fillColor",fillColor)
            args.putInt("borderColor", borderColor)
            intent.putExtras(args)
            startActivityForResult(intent, INTENT_EDIT_MIX_OBJECT)
        }
        else{
            var intent= Intent(this, AddObjectActivity::class.java)
            var data: ArrayList<Point2D> = ArrayList(vertices)
            var args = Bundle()
            args.putParcelableArrayList("data", data)
            args.putInt("fillColor", fillColor)
            args.putInt("borderColor", borderColor)
            intent.putExtras(args)
            startActivityForResult(intent, INTENT_EDIT_EXIST_OBJECT)
        }
    }
    fun defineObjectType(vertices: MutableList<Point2D>, isConcave: Boolean) : ObjectType{
        if(vertices.size < 1)
            return ObjectType.NULL
        else if(vertices.size == 1)
            return ObjectType.POINT
        else if(vertices.size == 2)
            return ObjectType.LINE
        else if(vertices.size == 3)
            return  ObjectType.TRIANGLE
        else {
            if(isConcave)
                return ObjectType.CONCAVE_POLYGON
            else return ObjectType.POLYGON
        }
    }

    enum class UpdateType{
        ADD, EDIT, DELETE
    }
}