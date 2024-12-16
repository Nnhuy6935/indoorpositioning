package com.example.indoorpositioning.canvas.mixObject

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import com.example.indoorpositioning.Helputil.StringHelper
import com.example.indoorpositioning.Interface.OnUpdateUndoAndRedo
import com.example.indoorpositioning.Model.Object
import com.example.indoorpositioning.Model.ObjectType
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.R
import com.example.indoorpositioning.canvas.normal.AddObjectActivity.PickColorObject
import com.google.android.material.bottomsheet.BottomSheetDialog
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class MixObjectActivity : AppCompatActivity(), View.OnClickListener, OnUpdateUndoAndRedo {
    lateinit var btnBack : ImageButton
    lateinit var btnUndo : ImageButton
    lateinit var btnRedo : ImageButton
    lateinit var btnCustom : ImageButton
    lateinit var btnAdd : Button
    lateinit var mixView : MixSurfaceView

    var editMode : Boolean = true
    var lineType : Boolean = true

    var fillColor : Int = Color.BLACK
    var borderColor : Int = Color.BLACK
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.mix_layout)
        btnCustom = findViewById(R.id.btnCustom)
        btnRedo = findViewById(R.id.btnMixRedo)
        btnUndo = findViewById(R.id.btnMixUndo)
        btnBack = findViewById(R.id.btnBack)
        btnAdd = findViewById(R.id.btnAdd)
        mixView = findViewById(R.id.mixView)
        mixView.setUpdateUndoAndRedoCallback(this)

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


        mixView.cleanHistory()
        disableUndo()
        disableRedo()


        btnAdd.setOnClickListener(this)
        btnCustom.setOnClickListener(this)
        btnRedo.setOnClickListener(this)
        btnUndo.setOnClickListener(this)
        btnBack.setOnClickListener(this)

    }

    override fun onClick(view: View?) {
        if(view?.id == R.id.btnBack){
            setResult(RESULT_CANCELED)
            finish()
        }
        else if(view?.id == R.id.btnAdd){
            var points = mixView.getSurfaceRenderer()!!.getListPoint()
            var lines = mixView.getSurfaceRenderer()!!.getListLine()


            var actualPoints : MutableList<Point2D>
            var data: ArrayList<Point2D>
            if(points.size != 0) {
                actualPoints = arrangeOrderOfVertices(points, lines)
                data = ArrayList(actualPoints)
            }else
                data = ArrayList(points)
            var resultIntent = Intent()
            var args = Bundle()
            args.putParcelableArrayList("data",data)
            args.putString("groupId",mixView.getGroupId())
            args.putInt("borderColor",borderColor)
            args.putInt("fillColor",fillColor)
            resultIntent.putExtras(args)
            setResult(RESULT_OK, resultIntent)
            finish()
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
            val switchLineType = view.findViewById<Switch>(R.id.switchTypeLine)
            switchDrawMode.isChecked = editMode
            switchLineType.isChecked = lineType


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
                mixView.setDrawMode(editMode)
            }
            switchLineType.setOnClickListener{
                lineType = switchLineType.isChecked
                mixView.setLineType(lineType)
            }
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.show()
        }
        else if(view?.id == R.id.btnMixRedo){
            mixView.handleActionRedo()
        }
        else if(view?.id == R.id.btnMixUndo){
            mixView.handleActionUndo()
        }
    }

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
        mixView.getSurfaceRenderer()!!.setFillColor(fillColor)
        mixView.getSurfaceRenderer()!!.setBorderColor(borderColor)
        var listLine = mixView.getSurfaceRenderer()!!.getListLine()
        for(line in listLine){
            line.setFillColorWithInt(fillColor)
            line.setBorderColorWithInt(borderColor)
            mixView.requestRender()
        }
    }

    /** Load vertices input from intent**/
    fun renderIntentData(vertices: MutableList<Point2D>){
        //set group id for edit object

        mixView.setGroupId(vertices[0].getGroupId())
        mixView.getSurfaceRenderer()!!.getListPoint().addAll(vertices)
        var i = 0
        var listPoint = mixView.getSurfaceRenderer()!!.getListPoint()
        // không tự động nối 2 điểm đầu tiên và cuối cùng
        while( i < listPoint.size - 1) {
            if(!listPoint[i].getIsBelongCurve()
                || !listPoint[(i + 1 + listPoint.size) % listPoint.size].getIsBelongCurve()){
                var line = Object(
                    ObjectType.LINE,
                    mutableListOf(
                        listPoint[i],
                        listPoint[(i + 1 + listPoint.size) % listPoint.size]
                    ),
                    StringHelper().createRandomObjectId()
                )
                line.setBorderColorWithInt(borderColor)
                line.setFillColorWithInt(fillColor)
                mixView.getSurfaceRenderer()!!.addLineWithoutAddHistory(line)
            }
            ++i
        }
    }

    fun arrangeOrderOfVertices(vertices: MutableList<Point2D>, lines: MutableList<Object>) : MutableList<Point2D>{

        var output : MutableList<MutableList<Point2D>> = mutableListOf()
        var i = 0
        while(i < vertices.size){
            if(vertices[i].getIsBelongCurve()){
                var j = i + 1
                while(j < vertices.size && vertices[j].getIsBelongCurve()){
                    ++j
                }
                output.add(vertices.subList(i,j).toMutableList())
                vertices.removeAll(vertices.subList(i,j))
                i = j + 1

            }
            else
                ++i
        }
        // arrange the order of remain point

        while(lines.size > 0){
            var remainPoint : MutableList<Point2D> = mutableListOf()
            var line = lines[0]
            remainPoint.add(line.getVertices()[0])
            remainPoint.add(line.getVertices()[1])
            lines.remove(line)
            var i = 0
            while(i < lines.size){
                if(lines[i].getVertices()[0].getId() == remainPoint[0].getId()){
                    remainPoint.add(0, lines[i].getVertices()[1])
                    lines.removeAt(i)
                    i  = 0
                    continue
                }
                else if(lines[i].getVertices()[1].getId() == remainPoint[0].getId()){
                    remainPoint.add(0, lines[i].getVertices()[0])
                    lines.removeAt(i)
                    i  = 0
                    continue
                }
                else if(lines[i].getVertices()[0].getId() == remainPoint[remainPoint.size - 1].getId()){
                    remainPoint.add(lines[i].getVertices()[1])
                    lines.removeAt(i)
                    i  = 0
                    continue
                }
                else if(lines[i].getVertices()[1].getId() == remainPoint[remainPoint.size - 1].getId()){
                    remainPoint.add(lines[i].getVertices()[0])
                    lines.removeAt(i)
                    i  = 0
                    continue
                }else ++i
            }
            if(remainPoint.size > 0) {
                output.add(remainPoint)
            }

        }
        var j = 0

        while(output.size > 1){
            var size0 = output[0].size
            var index = 1
            while(index < output.size){
                var sizeIdx= output[index].size
                if(output[index][0].getId() == output[0][0].getId()){
                    //reversed and add first
                    output[index].removeAt(0)
                    output[0].addAll(0, output[index].reversed())
                    output.removeAt(index)
                    break
                }else if(output[index][0].getId() == output[0][size0 - 1].getId()){
                    // add last
                    output[index].removeAt(0)
                    output[0].addAll(output[index])
                    output.removeAt(index)
                    break
                }else if(output[index][sizeIdx - 1].getId() == output[0][0].getId()){
                    // add first
                    output[index].removeLast()
                    output[0].addAll(0, output[index])
                    output.removeAt(index)
                    break
                }else if(output[index][sizeIdx - 1].getId() == output[0][size0 - 1].getId()){
                    //reversed and add last
                    output[index].removeLast()
                    output[0].addAll(output[index].reversed())
                    output.removeAt(index)
                    break
                }else ++index
            }
        }
        return output[0]
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