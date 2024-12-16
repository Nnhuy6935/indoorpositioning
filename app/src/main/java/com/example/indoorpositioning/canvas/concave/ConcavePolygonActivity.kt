package com.example.indoorpositioning.canvas.concave

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.Visibility
import com.example.indoorpositioning.Helputil.StringHelper
import com.example.indoorpositioning.Interface.OnUpdateUndoAndRedo
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.R
import com.example.indoorpositioning.canvas.normal.AddObjectActivity.PickColorObject
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.internal.VisibilityAwareImageButton
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

/**CONCAVE POLYGON: when 2 line is interrupt => define a point at the intersection**/
class ConcavePolygonActivity : AppCompatActivity(), View.OnClickListener, OnUpdateUndoAndRedo {
    lateinit var concaveView : ConcavePolygonSurfaceView
    lateinit var btnBack : ImageButton
    lateinit var btnUndo : ImageButton
    lateinit var btnRedo: ImageButton
    lateinit var btnAdd : Button
    lateinit var btnCustom : ImageButton
    var editMode : Boolean = true

    var fillColor : Int = Color.BLACK
    var borderColor : Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.concave_polygon_layout)
        concaveView = findViewById(R.id.concaveView)
        btnBack = findViewById(R.id.btnBack)
        btnUndo = findViewById(R.id.btnConcaveUndo)
        btnRedo = findViewById(R.id.btnConcaveRedo)
        btnAdd = findViewById(R.id.btnAdd)
        btnCustom = findViewById(R.id.btnCustom)
        concaveView.setUpdateUndoAndRedoCallback(this)

        val intent = intent
        val data = intent.extras?.getParcelableArrayList<Point2D>("data")
        val fill = intent.extras?.getInt("fillColor")
        val border = intent.extras?.getInt("borderColor")
        if(fill != null)
            fillColor = fill
        if(border != null)
            borderColor = border
        updateColorForRenderer()
        if(data != null){
            editMode = true
            renderIntentData(data)
        }


        concaveView.cleanHistory()
        disableUndo()
        disableRedo()
        btnBack.setOnClickListener(this)
        btnAdd.setOnClickListener(this)
        btnUndo.setOnClickListener(this)
        btnRedo.setOnClickListener(this)
        btnCustom.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        if(view?.id == R.id.btnBack){
            setResult(RESULT_CANCELED)
            finish()
        }
        else if(view?.id == R.id.btnAdd){
            var points = concaveView.getSurfaceRenderer()!!.getListPoint()
            var lines = concaveView.getSurfaceRenderer()!!.getListLine()

            //if lines size == 0 => not a concave polygon
            if(lines == null || lines.size <= 0){
                setResult(RESULT_CANCELED)
                finish()
                return
            }
            var actualResult : MutableList<Point2D>
            if(!concaveView.isHistoryAllMoveAction()){
                if(points.size > 3){
                    actualResult = rearrangePoints(points,lines)
                    // if the polygon is not closed/sealed => not a concave polygon
                    if( actualResult[0].getId() != actualResult[actualResult.size - 1].getId()){
                        setResult(RESULT_CANCELED)
                        finish()
                        return
                    }else{
                        actualResult.removeLast()
                    }

                    if(editMode)
                        actualResult = actualResult.reversed().toMutableList()
                }
                else actualResult = points
            }else actualResult = points

            if(actualResult == null)
                actualResult = mutableListOf()
            var data : ArrayList<Point2D> = ArrayList(actualResult)
            var resultIntent = Intent()
            var args = Bundle()
            args.putParcelableArrayList("data",data)
            args.putString("groupId",concaveView.getGroupId())
            args.putInt("borderColor",borderColor)
            args.putInt("fillColor",fillColor)
            resultIntent.putExtras(args)
            setResult(RESULT_OK, resultIntent)
            finish()
        }
        else if(view?.id == R.id.btnConcaveUndo){
            concaveView.handleActionUndo()
        }
        else if(view?.id == R.id.btnConcaveRedo){
            concaveView.handleActionRedo()
        }
        else if(view?.id == R.id.btnCustom){
            val dialog = BottomSheetDialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.polygon_bottom_sheet, null)
            val btnClose = view.findViewById<ImageButton>(R.id.btnCloseCustomTab)
            val btnPickBorderColor = view.findViewById<Button>(R.id.btnPickBorder)
            val btnPickFillColor = view.findViewById<Button>(R.id.btnPickFillColor)
            val resultBorderColor = view.findViewById<View>(R.id.resultPickBorder)
            val resultFillColor = view.findViewById<View>(R.id.resultPickFill)
            resultFillColor.setBackgroundColor(fillColor)
            resultBorderColor.setBackgroundColor(borderColor)
            val switchDrawMode = view.findViewById<Switch>(R.id.switchDrawMode)
            //invisible for drawtype mode(this mode is used for mix screen)
            val txtDrawType = view.findViewById<TextView>(R.id.txtDrawType)
            val groupDrawType = view.findViewById<LinearLayout>(R.id.groupLineType)
            txtDrawType.visibility = VisibilityAwareImageButton.GONE
            groupDrawType.visibility = VisibilityAwareImageButton.GONE


            switchDrawMode.isChecked = editMode


            btnClose.setOnClickListener{
                dialog.dismiss()
            }
            btnPickBorderColor.setOnClickListener{
                openColorPickerDialog(borderColor, resultBorderColor, PickColorObject.BORDER )
            }
            btnPickFillColor.setOnClickListener{
                openColorPickerDialog(fillColor, resultFillColor, PickColorObject.FILL)
            }
            switchDrawMode.setOnClickListener{
                editMode = switchDrawMode.isChecked
                concaveView.setDrawMode(editMode)
            }
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.show()
        }
    }

    /**parameter:
     *  originColor: color before change
     *  result: the view will store/display change result
     *  itemSetColor: type of object will change color (FILL or BORDER)
     **/
    fun openColorPickerDialog(originColor: Int, result: View , itemSetColor: PickColorObject) {
        val colorPickerDialog = AmbilWarnaDialog(this, originColor,
            object: OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    Log.d("== CANCEL PICK COLOR", "cancel")
                    result.setBackgroundColor(originColor)
                }
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    result.setBackgroundColor(color)
                    if(itemSetColor == PickColorObject.FILL){
                        fillColor = color
                    }else if(itemSetColor == PickColorObject.BORDER){
                        borderColor = color
                    }
                    updateColorForRenderer()
                }
            })
        colorPickerDialog.show()
    }
    fun updateColorForRenderer(){
        concaveView.getSurfaceRenderer()!!.setFillColor(fillColor)
        concaveView.getSurfaceRenderer()!!.setBorderColor(borderColor)
        var listLine = concaveView.getSurfaceRenderer()!!.getListLine()
        for(line in listLine){
            line.setFillColorWithInt(fillColor)
            line.setBorderColorWithInt(borderColor)
            concaveView.requestRender()
        }
    }
    /** Arrange list of vertices in count-lockwise to draw**/
    fun rearrangePoints(points: MutableList<Point2D>, lines: MutableList<Object>) : MutableList<Point2D>{
        var output : MutableList<Point2D> = mutableListOf()
        output.add(lines[0].getVertices()[0])
        output.add(lines[0].getVertices()[1])

        lines.remove(lines[0])
        while(lines.size > 0){
            var first = 0
            var second = 0
            if(numberOfLineRelated(output.get(output.size-1), lines) > numberOfLineRelated(output.get(0),lines)){
                first = 0
                second = output.size - 1
            }else{
                first = output.size - 1
                second = 0
            }
            //------------
            for(i in lines){
                var point1 = i.getVertices()[0]
                var point2 = i.getVertices()[1]
                var checkpoint1 = point1.getId() == output.get(first).getId()
                var checkpoint2 = point2.getId() == output.get(first).getId()
                if(checkpoint2 || checkpoint1){
                    if(checkpoint2) {
                        if(first != 0)
                            output.add(point1)
                        else output.add(0,point1)
                    }
                    else {
                        if(first != 0)
                            output.add(point2)
                        else output.add(0,point2)
                    }
                    lines.remove(i)
                    break
                }
            }
            //------------
            for(i in lines){
                var point1 = i.getVertices()[0]
                var point2 = i.getVertices()[1]
                var checkpoint1 = point1.getId() == output.get(second).getId()
                var checkpoint2 = point2.getId() == output.get(second).getId()
                if(checkpoint2 || checkpoint1){
                    if(checkpoint2){
                        if(second == 0)
                            output.add(0,point1)
                        else output.add(point1)
                    }
                    else {
                        if(second == 0)
                            output.add(0,point2)
                        else output.add(point2)
                    }
                    lines.remove(i)
                    break
                }
            }
        }
        if(checkLockwise(output))
            return output.reversed().toMutableList()
        else return  output
    }

    /** Signum Algorithm: check if input vertices is in lockwise orientation
     * REFERENCE: https://www.baeldung.com/cs/list-polygon-points-clockwise**/
    fun checkLockwise(vertices: MutableList<Point2D>) : Boolean{
        var sum = 0f
        var i = 0
        while(i < vertices.size - 1){
            var p1 = vertices[i]
            var p2 = vertices[i + 1]
            sum += p1.getX() * p2.getY() - p1.getY() * p2.getX()
            ++i
        }
        if(sum > 0) return false
        else return true
    }


    fun numberOfLineRelated(point: Point2D, lines : List<Object>) : Int{
        var output = 0
        for(i in lines){
            if(i.getVertices()[0].getId() == point.getId() || i.getVertices()[1].getId() == point.getId())
                ++output
        }
        return output
    }

    /** Load vertices input from intent (if had)**/
    fun renderIntentData(vertices: MutableList<Point2D>){
        //set group id for edit object
        concaveView.setGroupId(vertices[0].getGroupId())
        var i = 0
        while( i < vertices.size){
            if(!findExistPointInList(vertices[i]))
                concaveView.getSurfaceRenderer()!!.addPoint(vertices[i])
            ++i
        }
        var j = 1
        while(j < concaveView.getSurfaceRenderer()!!.getListPoint().size){
            var line = Object(ObjectType.LINE,
                mutableListOf(concaveView.getSurfaceRenderer()!!.getListPoint()[j], concaveView.getSurfaceRenderer()!!.getListPoint()[j-1]),
                StringHelper().createRandomObjectId())
            line.setFillColorWithInt(fillColor)
            line.setBorderColorWithInt(borderColor)
            concaveView.getSurfaceRenderer()!!.addLine(line)
            ++j
        }
        var line = Object(ObjectType.LINE,
            mutableListOf(
                concaveView.getSurfaceRenderer()!!.getListPoint()[0],
                concaveView.getSurfaceRenderer()!!.getListPoint()[concaveView.getSurfaceRenderer()!!.getListPoint().size-1]
            ),
            StringHelper().createRandomObjectId()
        )
        line.setBorderColorWithInt(borderColor)
        line.setFillColorWithInt(fillColor)
        concaveView.getSurfaceRenderer()!!.addLine(line)
    }
    /** Check if a point (provided with id) exists in render list point
     * if exists => insert with exists point
     * else insert a new point **/
    fun findExistPointInList(vertice : Point2D) : Boolean{
        for(v in concaveView.getSurfaceRenderer()!!.getListPoint()){
            if(v.getId() == vertice.getId()){
                concaveView.getSurfaceRenderer()!!.addPoint(v)
                return true
            }
        }
        return false
    }

    override fun enableUndo() {
        btnUndo.isEnabled = true
        btnUndo.setImageResource(R.drawable.baseline_undo_24)
    }

    override fun enableRedo() {
        btnRedo.isEnabled = true
        btnRedo.setImageResource(R.drawable.baseline_redo_24)
    }

    override fun disableUndo() {
        btnUndo.isEnabled = false
        btnUndo.setImageResource(R.drawable.baseline_undo_24_disable)
    }

    override fun disableRedo() {
        btnRedo.isEnabled = false
        btnRedo.setImageResource(R.drawable.baseline_redo_24_disable)
    }

}