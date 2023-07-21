package com.example.pbxtrackingtracingapp

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.camera.CameraSourceConfig
import com.google.mlkit.vision.camera.CameraXSource
import com.google.mlkit.vision.common.InputImage
import com.example.pbxtrackingtracingapp.databinding.ActivityQrScanningBinding
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

data class QRContent(val isPBXCode: Boolean,
                     val type: String,
                     val part_number : String,
                     val DD_number : String,
                     val serial_number: String)

class QrScanning : AppCompatActivity() {

    // Mit diesem Wert wird die Camera-Berechtigung angefragt
    private val requestCodeCameraPermission = 1001

    private lateinit var binding: ActivityQrScanningBinding

    // Erstelle eine Variable um die CameraSource zu halten
    private lateinit var cameraXSource: CameraXSource

    private lateinit var barcodeScanner: BarcodeScanning

    private lateinit var cameraSourceConfig : CameraSourceConfig

    public lateinit var barcodeString : String

    private lateinit var targetType : String

    private var serialNumbers = mutableListOf<String>()

    private val partnumber_TEV = "068U3715"

    private fun interpretBarcode(rawBarcode:String): QRContent{
        var isPBXCode : Boolean = true
        var type : String = ""
        var part_number : String = ""
        var DD_number : String = ""
        var serial_number : String = ""

        val identifier =  rawBarcode.subSequence(0, 3)

        // Frame
        if(identifier == "P01"){
            val barcode = rawBarcode.split("-")
            type = "frame"
            part_number = barcode[1]
            DD_number = "V200-040-000-0"
            serial_number = barcode[2]
        }

        // Compressor
        else if(identifier == "SHS"){
            type = "compressor"
            part_number = "800555"
            DD_number = "ME060000.001"
            serial_number = rawBarcode.split("-")[2]
        }

        // Evaporator
        else if(identifier=="R02"){
            type = "evaporator"
            part_number = "800591"
            DD_number = "ME060000.002"
            serial_number = rawBarcode.split(",")[1]
        }

        // Condenser
        else if (identifier=="[)>"){
            type = "condenser"
            part_number = "800617"
            DD_number = "ME060000.020"
            val separator : String = Character.toString(29.toChar())
            serial_number = rawBarcode.split(separator)[2]
        }

        else{
            isPBXCode = false
        }
        return QRContent(isPBXCode, type, part_number, DD_number, serial_number)
    }

    private fun isTargetType(targetType:String, readType:String): Boolean{
        if (targetType == "frame"){
            if (readType == "frame"){
                return true
            }
        }

        else if (targetType == "cmp"){
            val cmpList = listOf<String>("compressor", "evaporator", "condenser")
            if (cmpList.contains(readType)){
                return true
            }
        }
        return false
    }
    /**
     * Diese Funktion wird als Callback auf den Button "buttonBack" hinterlegt.
     */
    private val listenerButtonBack = View.OnClickListener { ImageView ->
        when (ImageView.id) {
            R.id.buttonBack -> {
                closeAll()
                finish()
            }
        }
    }

    /** Diese Funktion wird beim Start der Activity (Scannen-Seite) aufgerufen. Es werden folgende
     *  Aufgaben abgearbeitet:
     *  - Button zum Beenden erstellt
     *  - Falls noch nicht vorhanden: Kamera-Rechte beantragt
     *  - Kamera- und Bar/QR-Code-Detector-Handler erstellt und eingerichtet
     *
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScanningBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Lade die Referenz des Buttons und verbinde diesen mit dem Listener
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener(listenerButtonBack)

        // Wenn keine Kamera-Berechtigung vorliegt, dann ...
        if (ActivityCompat.checkSelfPermission(this@QrScanning, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Beantrage die Kamera-Berechtigung
            askForCameraPermission()

        } else {

            // Richte Screen und die Kamera ein um den QR-Scan-Modus zu starten
            setupControls()
        }

        targetType = getIntent().getExtras()?.getString("targetType").toString()
        println("targetType")
        println(targetType)
    }

    /** In dieser Funktion werden folgenden Aufgaben abgearbeitet:
     *   - Erstelle Bar/QR-Code-Detector
     *   - Erstelle die Kamera-Referenz und ver-linke diese mit dem Bar/QR-Code-Detector
     *   - Binde auf das Layout "cameraSurfaceView" die Kamera-Callbacks
     *   - Definiere die Callbacks wenn ein Bar/QR-Code gelesen werden konnte
     */
    private fun setupControls() {

        // Lade eine Referenz auf das PreviewView-Widget
        val cameraSurfaceView = findViewById<PreviewView>(R.id.cameraSurfaceView)

        // Erstelle eine Referenz auf die BarcodeDetector, definiere den QR-Code-Modus
        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )

        // Erstelle den OCR Recognizer
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        cameraSourceConfig = CameraSourceConfig.Builder(this, barcodeScanner) {
            // QR Scanning
            val task: Task<MutableList<Barcode>> = barcodeScanner.process(InputImage.fromBitmap(cameraSurfaceView.bitmap!!, 0))
            task.addOnSuccessListener { barcodes ->

                if (barcodes.size != 0) {
                    println("Raw barcode")
                    println("barcode ${barcodes[0].displayValue}")
                    println("barcode ${barcodes[0].rawValue}")

                    // Check if the QR Code is correct for PBX components
                    val barcodeContent: QRContent = interpretBarcode(barcodes[0].rawValue.toString())
                    println(barcodeContent.type)
                    println("targetType $targetType")
                    if(barcodeContent.isPBXCode && isTargetType(targetType, barcodeContent.type)){
                        val data = Intent()
                        data.putExtra("type", barcodeContent.type)
                        data.putExtra("part_number", barcodeContent.part_number)
                        data.putExtra("serial_number", barcodeContent.serial_number)
                        data.putExtra("DD_number", barcodeContent.DD_number)

                        setResult(Activity.RESULT_OK, data)

                        closeAll()
                        finish()
                    }
                }
            }

            // OCR Scanning
            val result = recognizer.process(InputImage.fromBitmap(cameraSurfaceView.bitmap!!, 0))
            result.addOnSuccessListener {
                analyseTextObject(it)
            }
        }
            .setFacing(CameraSourceConfig.CAMERA_FACING_BACK)
            .build()

        cameraXSource = CameraXSource(cameraSourceConfig, cameraSurfaceView)


        // Wenn keine Kamera-Berechtigung vorliegt, dann ...
        if (ActivityCompat.checkSelfPermission(this@QrScanning, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Beantrage die Kamera-Berechtigung
            askForCameraPermission()

        } else {

            // Richte Screen und die Kamera ein um den QR-Scan-Modus zu starten
            cameraXSource.start()
        }
    }
    /**
     * Diese Funktion sucht im Text Object nach den akzeptierten Partnumbers und fügt eine gefundene Seriennummer einer Liste hinzu
     */
    private fun analyseTextObject(result: com.google.mlkit.vision.text.Text){
        val resultText = result.text

        // Check if the TEV partnumber is in the string
        if (partnumber_TEV in resultText){
            // look for the index of the string, because the batch number usually is displayed right after the partnumber
            val indexOfPN = resultText.indexOf(partnumber_TEV)
            val serialnumber = resultText.subSequence(indexOfPN+9, indexOfPN+14).toString()

            // check if the found sequence is all numeric characters, if yes, add it to the list
            if(isNumeric(serialnumber)){
                // check, if the serialnumber is already entered twice in the list
                val occurences = serialNumbers.groupingBy { serialnumber }.eachCount()[serialnumber]
                if ( occurences == 2){
                    Log.d(ContentValues.TAG, "Third entry of serialnumber: "+serialnumber)
                    val data = Intent()
                    data.putExtra("type", "tev")
                    data.putExtra("part_number", "800601")
                    data.putExtra("serial_number", serialnumber)
                    data.putExtra("DD_number", "ME060000.013")

                    setResult(Activity.RESULT_OK, data)

                    closeAll()
                    finish()
                }
                serialNumbers.add(0, serialnumber)

                if (serialNumbers.size > 5) {
                    serialNumbers.removeLast()
                }
            }
            Log.d(ContentValues.TAG, result.text)
            Log.d(ContentValues.TAG, result.text)


        }

    }

    private fun isNumeric(toCheck: String): Boolean {
        return toCheck.all { char -> char.isDigit() }
    }
    /** Diese Funktion beantragt vom User die Rechte um auf die Kamera zuzugreifen
     */
    private fun askForCameraPermission() {
        // Beantrage die Rechte um auf die Kamera zuzugreifen
        ActivityCompat.requestPermissions(this@QrScanning, arrayOf(Manifest.permission.CAMERA), requestCodeCameraPermission)
    }

    /** Mit dieser Funktion werden alle erstellten/gestarteten Elemente dieser Seite beendet/geschlossen
     */
    private fun closeAll() {

        if (this::cameraXSource.isInitialized) {

            // Beende alles
            cameraXSource.stop()
            cameraXSource.close()
        }
    }

    /** Diese Funktion wird seitens des OS gerufen wenn vom User die Kamera-Rechte-Entscheidung getroffen wurde
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Wenn der "requestCode" gleich den von der APP ist und im "grantResults" etwas steht, dann ...
        if (requestCode == requestCodeCameraPermission && grantResults.isNotEmpty()) {

            // Wenn im "grantResults" auf der ersten Stelle der Wert "PackageManager.PERMISSION_GRANTED" zu finden ist, dann ...
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                setupControls()

                // Erstelle/Starte diese Activity neu
                this.recreate()

            } else {

                // Gib am Display eine Text-Meldung, das die Berechtigung notwendig ist, aus.
                Toast.makeText(this@QrScanning, "Gibst du mir Kamera!", Toast.LENGTH_LONG).show()

                // Erstelle/Starte diese Activity neu, um dem User nochmals die Berechtigung-Abfrage anzuzeigen
                this.recreate()
            }
        }
    }

    /** Diese Funktion wird gerufen wenn die Activity beendet wird
     */
    override fun onDestroy() {
        super.onDestroy()
        // Beende alles
        closeAll()
    }

    override fun onPause() {
        super.onPause()
        // Beende alles
        closeAll()
    }

    override fun onResume() {
        super.onResume()

        // Wenn keine Kamera-Berechtigung vorliegt, dann ...
        if (ActivityCompat.checkSelfPermission(this@QrScanning, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            // Beantrage die Kamera-Berechtigung
            askForCameraPermission()

        } else {

            // Richte Screen und die Kamera ein um den QR-Scan-Modus zu starten
            setupControls()
        }
    }
}
