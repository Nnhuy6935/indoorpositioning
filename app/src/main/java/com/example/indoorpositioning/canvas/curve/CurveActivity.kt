package com.example.indoorpositioning.canvas.curve

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.indoorpositioning.Interface.OnUpdateUndoAndRedo
import com.example.indoorpositioning.Model.Point2D
import com.example.indoorpositioning.R
import com.example.indoorpositioning.canvas.normal.AddObjectActivity.PickColorObject
import com.google.android.material.bottomsheet.BottomSheetDialog
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener

class CurveActivity : AppCompatActivity(), View.OnClickListener, OnUpdateUndoAndRedo {
    lateinit var btnBack : ImageButton
    lateinit var btnUndo : ImageButton
    lateinit var btnRedo : ImageButton
    lateinit var btnCustom : ImageButton
    lateinit var btnAdd: Button
    lateinit var curveView : CurveSurfaceView
    var fillColor : Int = Color.BLACK
    var borderColor : Int = Color.BLACK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.curve_layout)
        btnBack = findViewById(R.id.btnBack)
        btnUndo = findViewById(R.id.btnCurveUndo)
        btnRedo = findViewById(R.id.btnCurveRedo)
        btnAdd = findViewById(R.id.btnAddCurve)
        curveView = findViewById(R.id.curView)
        btnCustom = findViewById(R.id.btnCustom)
        curveView.setUpdateUndoAndRedoCallback(this)
        curveView.getSurfaceRenderer()!!.setBorderColor(borderColor)
        curveView.getSurfaceRenderer()!!.setFillColor(fillColor)

        val data = intent?.extras?.getParcelableArrayList<Point2D>("data")
        val fill = intent?.extras?.getInt("fillColor")
        val border = intent?.extras?.getInt("borderColor")
        if(fill != null)
            fillColor = fill
        if(border != null)
            borderColor = border
        if(data != null && data.size > 0){
            renderIntentData(data)
        }
        updateColorToRenderer()


        curveView.cleanHistory()
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
        if(view?.id == R.id.btnAddCurve){
            var output : List<Point2D> = curveView.getSurfaceRenderer()!!.getListPoint()
            if(output == null ){
                setResult(RESULT_CANCELED)
                finish()
            }else{
                var vertices = output.toMutableList()
                var data : ArrayList<Point2D> = ArrayList(vertices)
                var resultIntent = Intent()
                var args = Bundle()
                args.putParcelableArrayList("data",data)
                args.putString("groupId",curveView.getGroupId())
                args.putInt("borderColor",borderColor)
                args.putInt("fillColor",fillColor)
                resultIntent.putExtras(args)
                setResult(RESULT_OK,resultIntent)
                finish()
            }
        }
        if(view?.id == R.id.btnCurveUndo){
            curveView.handleActionUndo()
        }
        if(view?.id == R.id.btnCurveRedo){
            curveView.handleActionRedo()
        }
        if(view?.id == R.id.btnCustom){
            val dialog = BottomSheetDialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.object_bottom_sheet, null)
            val btnClose = view.findViewById<ImageButton>(R.id.btnCloseCustomTab)
            val btnPickBorderColor = view.findViewById<Button>(R.id.btnPickBorder)
            val btnPickFillColor = view.findViewById<Button>(R.id.btnPickFillColor)
            val resultBorderColor = view.findViewById<View>(R.id.resultPickBorder)
            val resultFillColor = view.findViewById<View>(R.id.resultPickFill)
            resultFillColor.setBackgroundColor(fillColor)
            resultBorderColor.setBackgroundColor(borderColor)


            btnClose.setOnClickListener{
                dialog.dismiss()
            }
            btnPickBorderColor.setOnClickListener{
                openColorPickerDialog(borderColor, resultBorderColor, PickColorObject.BORDER )
            }
            btnPickFillColor.setOnClickListener{
                openColorPickerDialog(fillColor, resultFillColor, PickColorObject.FILL)
            }
            dialog.setCancelable(false)
            dialog.setContentView(view)
            dialog.show()
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
                    updateColorToRenderer()
                }
            })
        colorPickerDialog.show()
    }
    fun updateColorToRenderer(){
        curveView.getSurfaceRenderer()!!.setBorderColor(borderColor)
        curveView.getSurfaceRenderer()!!.setFillColor(fillColor)
        curveView.requestRender()
    }
    fun renderIntentData(vertices: MutableList<Point2D>){
        curveView.setGroupId(vertices[0].getGroupId())
        var i = 0
        while( i < vertices.size ) {
            curveView.getSurfaceRenderer()!!.addPoint(vertices[i])
            ++i
        }
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