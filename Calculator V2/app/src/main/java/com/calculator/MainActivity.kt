package com.calculator

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    // Represent whether the lastly pressed key is numeric or not
    var lastNumeric: Boolean = false

    // If true, do not allow to add another DOT
    var lastDot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        //This call the parent constructor
        super.onCreate(savedInstanceState)
        // This is used to align the xml view to this class
        setContentView(R.layout.activity_main)
        /*This is implemented by android studio it self when we select the Basic Activity while creating the project.*/
        setSupportActionBar(toolbar)

        // KP : Check write permission first.
        if(!isReadStorageAllowed()){
            requestStoragePermission()
        }

        Toast.makeText(this, "Welcome krub.", Toast.LENGTH_SHORT).show()

        btn_SaveAndShare.setOnClickListener{
            //Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show()
            if(isReadStorageAllowed()){
                //Run over code here
                BitmapAsyncTask(getBitmapFromView(tvInput)).execute()
            }else{
                requestStoragePermission()
            }

        }

    }

    /**
     * Appends the numeric Button.text to the TextView
     */
    fun onDigit(view: View) {
        // text of button is appended to textView
        tvInput.append((view as Button).text)

        // Set the flag
        lastNumeric = true
    }

    /**
     * Append . to the TextView
     */
    fun onDecimalPoint(view: View) {

        // If the last appeded value is numeric then appen(".") or don't.
        if (lastNumeric && !lastDot) {
            tvInput.append(".")
            lastNumeric = false // Update the flag
            lastDot = true // Update the flag
        }
    }

    /**
     * Append +,-,*,/ operators to the TextView as per the Button.Text
     */
    fun onOperator(view: View) {
        if (lastNumeric && !isOperatorAdded(tvInput.text.toString())) {
            tvInput.append((view as Button).text)
            lastNumeric = false // Update the flag
            lastDot = false    // Reset the DOT flag
        }
    }

    /**
     * Clear the TextView
     */
    fun onClear(view: View) {
        tvInput.text = ""
        lastNumeric = false // Reset the flag
        lastDot = false // Reset the flag
    }

    /**
     * Calculate the output
     */
    fun onEqual(view: View) {
        // If the last input is a number only, solution can be found.
        if (lastNumeric) {
            // Read the textView value
            var value = tvInput.text.toString()

            var prefix = ""
            try {

                // Here if the value starts with '-' then we will separate it and perform the calculation with value.
                if (value.startsWith("-")) {
                    prefix = "-"
                    value = value.substring(1);
                }

                // If the inputValue contains the Division operator
                if (value.contains("/")) {
                    // Will split the inputValue using Division operator
                    val splitedValue = value.split("/")

                    var one = splitedValue[0] // Value One
                    val two = splitedValue[1] // Value Two

                    if (!prefix.isEmpty()) { // If the prefix is not empty then we will append it with first value i.e one.
                        one = prefix + one
                    }

                    /*Here as the value one and two will be calculated based on the operator and
                    if the result contains the zero after decimal point will remove it.
                    And display the result to TextView*/
                    tvInput.text = removeZeroAfterDot((one.toDouble() / two.toDouble()).toString())
                } else if (value.contains("*")) {
                    // If the inputValue contains the Multiplication operator
                    // Will split the inputValue using Multiplication operator
                    val splitedValue = value.split("*")

                    var one = splitedValue[0] // Value One
                    val two = splitedValue[1] // Value Two

                    if (!prefix.isEmpty()) { // If the prefix is not empty then we will append it with first value i.e one.
                        one = prefix + one
                    }

                    /*Here as the value one and two will be calculated based on the operator and
                    if the result contains the zero after decimal point will remove it.
                    And display the result to TextView*/
                    tvInput.text = removeZeroAfterDot((one.toDouble() * two.toDouble()).toString())
                } else if (value.contains("-")) {

                    // If the inputValue contains the Subtraction operator
                    // Will split the inputValue using Subtraction operator
                    val splitedValue = value.split("-")

                    var one = splitedValue[0] // Value One
                    val two = splitedValue[1] // Value Two

                    if (!prefix.isEmpty()) { // If the prefix is not empty then we will append it with first value i.e one.
                        one = prefix + one
                    }

                    /*Here as the value one and two will be calculated based on the operator and
                    if the result contains the zero after decimal point will remove it.
                    And display the result to TextView*/
                    tvInput.text = removeZeroAfterDot((one.toDouble() - two.toDouble()).toString())
                } else if (value.contains("+")) {
                    // If the inputValue contains the Addition operator
                    // Will split the inputValue using Addition operator
                    val splitedValue = value.split("+")

                    var one = splitedValue[0] // Value One
                    val two = splitedValue[1] // Value Two

                    if (!prefix.isEmpty()) { // If the prefix is not empty then we will append it with first value i.e one.
                        one = prefix + one
                    }

                    /*Here as the value one and two will be calculated based on the operator and
                    if the result contains the zero after decimal point will remove it.
                    And display the result to TextView*/
                    tvInput.text = removeZeroAfterDot((one.toDouble() + two.toDouble()).toString())
                }
            } catch (e: ArithmeticException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * It is used to check whether any of the operator is used or not.
     */
    private fun isOperatorAdded(value: String): Boolean {

        /**
         * Here first we will check that if the value starts with "-" then will ignore it.
         * As it is the result value and perform further calculation.
         */

        return if (value.startsWith("-")) {
            false
        } else {
            (value.contains("/")
                    || value.contains("*")
                    || value.contains("-")
                    || value.contains("+"))
        }
    }

    /**
     * Remove the zero after decimal point
     */
    private fun removeZeroAfterDot(result: String): String {

        var value = result

        if (result.contains(".0")) {
            value = result.substring(0, result.length - 2)
        }

        return value
    }

    private fun requestStoragePermission() {


        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(this, "Need permission to add a Background", Toast.LENGTH_SHORT).show()
        }
        //And finally ask for the permission
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@MainActivity,
                    "Permission granted now you can read the storage files.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(
                    this@MainActivity,
                    "Oops you just denied the permission.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {

        val result = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        )

        //If permission is granted returning true and If permission is not granted returning false
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitmapFromView(view: View): Bitmap {

        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        //Bind a canvas to it
        val canvas = Canvas(returnedBitmap)
        //Get the view's background
        val bgDrawable = view.background
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas)
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE)
        }
        // draw the view on the canvas
        view.draw(canvas)
        //return the bitmap
        return returnedBitmap
    }


    private inner class BitmapAsyncTask(val mBitmap: Bitmap?) :
        AsyncTask<Any, Void, String>() {

        @Suppress("DEPRECATION")
        private var mDialog: ProgressDialog? = null

        override fun onPreExecute() {
            super.onPreExecute()

            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any): String {

            var result = ""

            if (mBitmap != null) {

                try {
                    val bytes = ByteArrayOutputStream() // Creates a new byte array output stream.
                    // The buffer capacity is initially 32 bytes, though its size increases if necessary.

                    mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)

                    val f = File(
                        externalCacheDir!!.absoluteFile.toString()
                                + File.separator + "Custom_Calculator_" + System.currentTimeMillis() / 1000 + ".jpg"
                    )

                    val fo =
                        FileOutputStream(f) // Creates a file output stream to write to the file represented by the specified object.
                    fo.write(bytes.toByteArray()) // Writes bytes from the specified byte array to this file output stream.
                    fo.close() // Closes this file output stream and releases any system resources associated with this stream. This file output stream may no longer be used for writing bytes.
                    result = f.absolutePath // The file absolute path is return as a result.
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            cancelProgressDialog()

            if (!result.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            MediaScannerConnection.scanFile(
                this@MainActivity, arrayOf(result), null
            ) { path, uri ->
                // This is used for sharing the image after it has being stored in the storage.
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(
                    Intent.EXTRA_STREAM,
                    uri
                ) // A content: URI holding a stream of data associated with the Intent, used to supply the data being sent.
                shareIntent.type =
                    "image/jpeg" // The MIME type of the data being handled by this intent.
                startActivity(
                    Intent.createChooser(
                        shareIntent,
                        "Share"
                    )
                )
            }
            // END
        }

        /**
         * This function is used to show the progress dialog with the title and message to user.
         */
        private fun showProgressDialog() {
            @Suppress("DEPRECATION")
            mDialog = ProgressDialog.show(
                this@MainActivity,
                "",
                "Saving your image..."
            )
        }

        /**
         * This function is used to dismiss the progress dialog if it is visible to user.
         */
        private fun cancelProgressDialog() {
            if (mDialog != null) {
                mDialog!!.dismiss()
                mDialog = null
            }
        }
    }

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
    }
}