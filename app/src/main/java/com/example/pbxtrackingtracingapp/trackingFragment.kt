
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.pbxtrackingtracingapp.QrScanning
import com.example.pbxtrackingtracingapp.R
import java.nio.charset.Charset

class trackingFragment:Fragment(R.layout.fragment_tracking) {

//  FRAME SCANNING
    private val intentLauncherFrame =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // update the TextView fields
                view?.findViewById<TextView>(R.id.FramePNText)!!.text = result.data?.getStringExtra("part_number").toString()
                view?.findViewById<TextView>(R.id.FrameSNText)!!.text = result.data?.getStringExtra("serial_number").toString()
                view?.findViewById<TextView>(R.id.FrameDDText)!!.text = result.data?.getStringExtra("DD_number").toString()
            }

        }

    private val listenerScanFrame = View.OnClickListener { ImageView ->
        when (ImageView.id){
            R.id.scanFrameBtn -> {
                val myIntent = Intent(requireContext(), QrScanning::class.java)
                var foo_bundle = Bundle()
                foo_bundle.putString("targetType", "frame")
                intentLauncherFrame.launch(myIntent.putExtras(foo_bundle))
            }
        }
    }

//  COMPONENT SCANNING
private val intentLauncherComponent =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // update the TextView fields
            view?.findViewById<TextView>(R.id.CmpTypeText)!!.text = result.data?.getStringExtra("type").toString()
            view?.findViewById<TextView>(R.id.CmpPNText)!!.text = result.data?.getStringExtra("part_number").toString()
            view?.findViewById<TextView>(R.id.CmpSNText)!!.text = result.data?.getStringExtra("serial_number").toString()
            view?.findViewById<TextView>(R.id.CmpDDText)!!.text = result.data?.getStringExtra("DD_number").toString()
        }

    }

    private val listenerScanCmp = View.OnClickListener { ImageView ->
        when (ImageView.id){
            R.id.scanCmpBtn -> {
                val myIntent = Intent(requireContext(), QrScanning::class.java)
                var foo_bundle = Bundle()
                foo_bundle.putString("targetType", "cmp")
                intentLauncherComponent.launch(myIntent.putExtras(foo_bundle))
            }
        }
    }

    private val listenerSendLink = View.OnClickListener { ImageView ->
        when (ImageView.id){
            R.id.link_Button -> {
                postComponentLink()
                clearTextFields()
            }
        }
    }

    private fun postComponentLink(){
        lateinit var responseString : String
        val queue = Volley.newRequestQueue(requireContext())
        val url =
            "https://script.google.com/macros/s/AKfycbzJauAnAvlcauQM1fxXw5YC1HNCduSZXMQ0hetVR0ilATK0zAjwH7rNKS6VkoVJzvqeIg/exec"

        val requArray = listOf<String>(
            "action=linkComponents",
            "frame_pn="+view?.findViewById<TextView>(R.id.FramePNText)!!.text.toString(),
            "frame_sn="+view?.findViewById<TextView>(R.id.FrameSNText)!!.text.toString(),
            "cmp_sn="+view?.findViewById<TextView>(R.id.CmpSNText)!!.text.toString(),
            "cmp_pn="+view?.findViewById<TextView>(R.id.CmpPNText)!!.text.toString()
        )
        val requestBody = requArray.joinToString(separator = "&")
        val stringReq: StringRequest =
            object : StringRequest(
                Method.POST, url,
                Response.Listener { response ->
                    // response
                    val strResp = response.toString()
                    Log.d("API", strResp)

                },
                Response.ErrorListener { error ->
                    Log.d("API", "error => $error")
                }
            ) {
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        stringReq.setRetryPolicy(DefaultRetryPolicy(10000, 5, 1.0F))
        queue.add(stringReq)
    }

    private fun clearTextFields(){
        view?.findViewById<TextView>(R.id.FramePNText)!!.text = ""
        view?.findViewById<TextView>(R.id.FrameDDText)!!.text = ""
        view?.findViewById<TextView>(R.id.FrameSNText)!!.text = ""
        view?.findViewById<TextView>(R.id.CmpTypeText)!!.text = ""
        view?.findViewById<TextView>(R.id.CmpPNText)!!.text = ""
        view?.findViewById<TextView>(R.id.CmpDDText)!!.text = ""
        view?.findViewById<TextView>(R.id.CmpSNText)!!.text = ""
        view?.findViewById<Button>(R.id.link_Button)!!.isEnabled = false
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            view?.findViewById<Button>(R.id.link_Button)!!.isEnabled = view?.findViewById<TextView>(R.id.FrameSNText)!!.text != "" && view?.findViewById<TextView>(R.id.CmpSNText)!!.text != ""
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // add listener to scan frame button
        view.findViewById<Button>(R.id.scanFrameBtn).setOnClickListener(listenerScanFrame)

        // add listener to scan component button
        view.findViewById<Button>(R.id.scanCmpBtn).setOnClickListener(listenerScanCmp)

        // add listener to send link button
        view.findViewById<Button>(R.id.link_Button).setOnClickListener(listenerSendLink)


        // add textWatcher
        view.findViewById<TextView>(R.id.CmpSNText).addTextChangedListener(textWatcher)
        view.findViewById<TextView>(R.id.FrameSNText).addTextChangedListener(textWatcher)
    }


}