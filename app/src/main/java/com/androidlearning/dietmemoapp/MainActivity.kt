package com.androidlearning.dietmemoapp

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    val dataModelList = mutableListOf<DataModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database = Firebase.database
        val myRef = database.getReference("my memo")

        val listView = findViewById<ListView>(R.id.mainLV)
        val listadapter = ListViewAdapter(dataModelList)
        listView.adapter = listadapter

        myRef.child(Firebase.auth.currentUser!!.uid).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                dataModelList.clear()
                for(dataModel in snapshot.children) {
                    Log.d("Data", dataModel.toString())
                    dataModelList.add(dataModel.getValue(DataModel::class.java)!!)
                }
                listadapter.notifyDataSetChanged()
                Log.d("DataModel", dataModelList.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        val writeButton = findViewById<ImageView>(R.id.writeBtn)
        writeButton.setOnClickListener{
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("운동 메모 다이얼로그")

            val mAlertDialog = mBuilder.show()

            var dateText = ""

            mAlertDialog.findViewById<Button>(R.id.dateSelectBtn)?.setOnClickListener{
                    val today = GregorianCalendar()
                    val year : Int = today.get(Calendar.YEAR)
                    val month : Int = today.get(Calendar.MONTH)
                    val date : Int = today.get(Calendar.DATE)

                    val dlg = DatePickerDialog(this, object : DatePickerDialog.OnDateSetListener{
                        override fun onDateSet(
                            view: DatePicker?,
                            year: Int,
                            month: Int,
                            dayOfMonth: Int
                        ) {
                            Log.d("MAIN", "${year}, ${month + 1}, ${dayOfMonth}")
                            val DateSelectBtn = mAlertDialog.findViewById<Button>(R.id.dateSelectBtn)
                            DateSelectBtn?.setText("${year}, ${month + 1}, ${dayOfMonth}")
                            dateText = "${year}, ${month + 1}, ${dayOfMonth}"

                        }

                    }, year, month, date)
                    dlg.show()
            }

            val saveBtn = mAlertDialog.findViewById<Button>(R.id.saveBtn)
            saveBtn?.setOnClickListener{
                val healMemo = mAlertDialog.findViewById<EditText>(R.id.healthMemo)?.text.toString()
                val database = Firebase.database
                val myRef = database.getReference("my memo").child(Firebase.auth.currentUser!!.uid)

                val model = DataModel(dateText, healMemo)
                myRef
                    .push()
                    .setValue(model)
                mAlertDialog.dismiss()
            }

        }
    }
}