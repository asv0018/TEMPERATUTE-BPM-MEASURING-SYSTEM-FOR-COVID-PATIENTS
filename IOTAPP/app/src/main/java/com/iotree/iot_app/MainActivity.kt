package com.iotree.iot_app

import android.content.Context
import android.media.MediaPlayer
import android.os.*
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.firebase.database.*
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object{
        val myListData: MutableList<HistoryData> = mutableListOf()

    }
    private lateinit var database: DatabaseReference
    lateinit var progresbar : ProgressBar
    lateinit var current_bpm_view:TextView
    lateinit var current_temperature_view:TextView
    lateinit var current_info_view:TextView
    var data_requested = false
    lateinit var timer:CountDownTimer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        database = FirebaseDatabase.getInstance().reference
        current_bpm_view = findViewById(R.id.cur_bpm_view)
        current_temperature_view = findViewById(R.id.cur_temp_view)
        current_info_view = findViewById(R.id.cur_info_view)
        val recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
        val adapter = HistoryAdapter(myListData)
        current_temperature_view.visibility = View.INVISIBLE
        current_bpm_view.visibility = View.INVISIBLE
        current_info_view.visibility = View.INVISIBLE
        progresbar = findViewById(R.id.progressBar) as ProgressBar
        progresbar.visibility = View.VISIBLE
        // register the floating action Button
        // register the floating action Button
        val extendedFloatingActionButton =
            findViewById<ExtendedFloatingActionButton>(R.id.floatingActionButton2)

        // register the nestedScrollView from the main layout

        // register the nestedScrollView from the main layout
        val nestedScrollView =
            findViewById<NestedScrollView>(R.id.nestedscrollview)

        // handle the nestedScrollView behaviour with OnScrollChangeListener
        // to hide or show the Floating Action Button

        // handle the nestedScrollView behaviour with OnScrollChangeListener
        // to hide or show the Floating Action Button
        nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY -> // the delay of the extension of the FAB is set for 12 items
            if (scrollY > oldScrollY + 12 && extendedFloatingActionButton.isShown) {
                extendedFloatingActionButton.hide()
            }

            // the delay of the extension of the FAB is set for 12 items
            if (scrollY < oldScrollY - 12 && !extendedFloatingActionButton.isShown) {
                extendedFloatingActionButton.show()
            }

            // if the nestedScrollView is at the first item of the list then the
            // floating action should be in show state
            if (scrollY == 0) {
                extendedFloatingActionButton.show()
            }
        })


        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        extendedFloatingActionButton.setOnClickListener{
            if (Build.VERSION.SDK_INT >= 26) {
                vibrator.vibrate(VibrationEffect.createOneShot(90, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(90)
            }
            database.child("PARAMETERS").child("is_data_requested").setValue(true)
            data_requested = true
            progresbar.visibility = View.VISIBLE
            timer = object: CountDownTimer(10000, 1) {
                override fun onTick(millisUntilFinished: Long) {
                    if (!data_requested){
                        progresbar.visibility = View.INVISIBLE
                        data_requested = false
                        cancel()

                    }
                }

                override fun onFinish() {
                    if(data_requested){
                        progresbar.visibility = View.INVISIBLE
                        database.child("PARAMETERS").child("is_data_requested").setValue(false)
                        var toast = Toast.makeText(this@MainActivity, "Unable to test at this moment!. Connect the device and try again.", Toast.LENGTH_LONG)
                        toast.setGravity(Gravity.CENTER, 0, 0)
                        toast.show()
                        data_requested = false

                    }
                }
            }
            timer.start()
        }

      /*  val check_requestbtn = FirebaseDatabase.getInstance().reference.child("PARAMETERS")
        check_requestbtn.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val flag = snapshot.child("is_data_requested").value as Boolean
                if (!flag){
                    if(data_requested){
                        timer.cancel()
                    }
                    data_requested = false
                }
            }

        })*/


        var current_data = FirebaseDatabase.getInstance().reference.child("REQUESTED_DATA")
        current_data.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val current_bpm = snapshot.child("HEART_BPM").value as Long
                val current_temp = snapshot.child("TEMPERATURE").value as Long
                val updated_date = snapshot.child("REQUESTED_DATE").value.toString()
                val updated_time = snapshot.child("REQUESTED_TIME").value.toString()
                current_temperature_view.text = "$current_temp \u2109"
                current_bpm_view.text = "$current_bpm bpm"
                current_info_view.text = "As tested on $updated_date at $updated_time"
                current_info_view.visibility = View.VISIBLE
                current_bpm_view.visibility = View.VISIBLE
                current_temperature_view.visibility = View.VISIBLE

                if(data_requested){
                    val toast = Toast.makeText(this@MainActivity, "Test is done successfully", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    progresbar.visibility = View.INVISIBLE
                }
                data_requested = false

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        var data = FirebaseDatabase.getInstance().reference.child("HISTORY")
        data.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    myListData.clear()
                    if(snapshot.hasChildren()){
                        for(i in snapshot.children){
                            val date = i.key
                            for(j in snapshot.child(date.toString()).children){
                                val time = j.key
                                val temp = snapshot.child(date.toString()).child(time.toString()).child("TEMPERATURE").value as Long
                                val bpm = snapshot.child(date.toString()).child(time.toString()).child("HEART_BPM").value as Long
                                myListData.add(HistoryData(temp,bpm,date.toString(),time.toString()))
                            }
                        }
                    }
                    myListData.removeAt(myListData.size-1)
                    myListData.reverse()
                    adapter.notifyDataSetChanged()
                    progresbar.visibility = View.INVISIBLE

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    override fun onBackPressed() {
        database.child("PARAMETERS").child("is_data_requested").setValue(false)
        super.onBackPressed()
    }

    override fun onPause() {
        if (data_requested){
            var toast = Toast.makeText(this@MainActivity, "You cannot run the test in the background.", Toast.LENGTH_SHORT)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
        database.child("PARAMETERS").child("is_data_requested").setValue(false)
        data_requested = false
        progresbar.visibility = View.INVISIBLE
        super.onPause()
    }

}