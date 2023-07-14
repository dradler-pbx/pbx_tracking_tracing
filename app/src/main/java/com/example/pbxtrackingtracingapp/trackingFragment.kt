
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.pbxtrackingtracingapp.QrScanning
import com.example.pbxtrackingtracingapp.R
import com.example.pbxtrackingtracingapp.requestNegativeFragment
import com.example.pbxtrackingtracingapp.requestPending
import com.example.pbxtrackingtracingapp.requestPositiveFragment
import org.w3c.dom.Text
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit


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
//                postComponentLink()
                postComponentLink()
                clearTextFields()
            }
        }
    }

    private fun postComponentLink(){
        val progressBar = ProgressBarHandler(requireContext())
        val errorView = ErrorPopupHandler(requireContext())

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
                    progressBar.hide()
                    if (strResp.subSequence(0, 3)=="200"){
                        Toast.makeText(requireContext(), strResp, Toast.LENGTH_LONG).show()
                    }
                    else{
                        errorView.show(strResp)
                    }


                },
                Response.ErrorListener { error ->
                    Log.d("API", "error => $error")
                    val errorString = error.toString()
                    progressBar.hide()
                    errorView.show(errorString)
                    Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show()
                }
            ) {
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }
            }
        stringReq.setRetryPolicy(DefaultRetryPolicy(10000, 5, 1.0F))
        queue.add(stringReq)
        progressBar.show()
    }

    private fun activateRequestPending() {
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(R.id.flFragment, requestPending())
            transaction.commit()
        }
    }

    private fun activateRequestPositive(response: String){
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(R.id.flFragment, requestPositiveFragment())
            transaction.commit()
        }

        Thread.sleep(500)
        if (transaction != null) {
            transaction.replace(R.id.flFragment, trackingFragment())
            transaction.commit()
        }
    }

    private fun activateRequestNegative(response: String, main_activity: Fragment){
        val transaction = activity?.supportFragmentManager?.beginTransaction()
        if (transaction != null) {
            transaction.replace(R.id.flFragment, requestNegativeFragment())
            transaction.commit()
        }
        Thread.sleep(500)
        if (transaction != null) {
            transaction.replace(R.id.flFragment, trackingFragment())
            transaction.commit()
        }
    }

    private fun postComponentLink_snychCall(): String{
        lateinit var response: String
        var future = RequestFuture.newFuture<String>()
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
            object : StringRequest(Method.POST, url, future, future) {
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray(Charset.defaultCharset())
                }

            }

        stringReq.setRetryPolicy(DefaultRetryPolicy(10000, 5, 1.0F))
        queue.add(stringReq)

        try {
            var response = future.get(10, TimeUnit.SECONDS)
        } catch (e: InterruptedException){

        }
        return response
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

/**
 * Diese Klasse stellt einen Progressbar bereit. Dieser kann am Screen eingeblendet werden
 * um dem User bei Lade/Arbeit-Schritten ein visuelles Feedback zu geben, dass die App
 * funktioniert und der noch warten soll.
 */
class ProgressBarHandler(mContext: Context) {

    private val mProgressBar: ProgressBar
    private lateinit var rl: RelativeLayout

    // Callback um sichtbar zu schalten
    fun show() {
        mProgressBar.visibility = View.VISIBLE
        rl.setBackgroundColor(Color.parseColor("#FFFFFF"))
    }

    // Callback um unsichtbar zu schalten
    fun hide() {
        mProgressBar.visibility = View.INVISIBLE
        rl.setBackgroundColor(Color.parseColor("#00000000"))
    }

    // Init-Callback
    init {

        // Lade das Layout auf welche der Progressbar eingebettet werden soll
        val layout = (mContext as Activity).findViewById<View>(android.R.id.content).rootView as ViewGroup

        // Erstelle "ProgressBar" und sichere die Referenz
        mProgressBar = ProgressBar(mContext, null, android.R.attr.progressBarStyleLarge)

        // Aktiviere Infinite-Animation
        mProgressBar.isIndeterminate = true

        // Erstelle ein RelativeLayout um die Abmessungen (x,y) zu definieren
        val params = RelativeLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
        )


        // Erstelle ein weiteres RelativeLayout umd die Position zu definieren
        rl = RelativeLayout(mContext)
        rl.gravity = Gravity.CENTER
        rl.addView(mProgressBar)
        rl.setBackgroundColor(Color.parseColor("#FFFFFF"))
//        rl.setBackgr
        // Ver-linke die Relative-Layout's miteinander
        layout.addView(rl, params)

        // Schalte den ProgressBar auf unsichtbar
        hide()
    }
}

/**
 * Diese Klasse stellt einen ErrorPopupHandler bereit. Dieser kann am Screen eingeblendet werden
 * um dem User bei Lade/Arbeit-Schritten ein visuelles Feedback zu geben, dass die App
 * funktioniert und der noch warten soll.
 */
class ErrorPopupHandler(mContext: Context) {

    private val errorTextView: TextView
    private lateinit var rl: RelativeLayout

    // Callback um sichtbar zu schalten
    fun show(errorText: String) {
        errorTextView.text = errorText+"\n\nClick on text to continue"
        errorTextView.visibility = View.VISIBLE
        errorTextView.setPadding(100,0,100,0)
        rl.setBackgroundColor(Color.parseColor("#d6471f"))
    }

    // Callback um unsichtbar zu schalten
    fun hide() {
        errorTextView.visibility = View.INVISIBLE
        rl.setBackgroundColor(Color.parseColor("#00000000"))
    }

    // Init-Callback
    init {

        // Lade das Layout auf welche der Progressbar eingebettet werden soll
        val layout = (mContext as Activity).findViewById<View>(android.R.id.content).rootView as ViewGroup

        // Erstelle "ProgressBar" und sichere die Referenz
        errorTextView = TextView(mContext, null)
        errorTextView.setTextColor(Color.parseColor("#000000"))
        errorTextView.setOnClickListener(View.OnClickListener { view -> hide() })

        // Erstelle ein RelativeLayout um die Abmessungen (x,y) zu definieren
        val params = RelativeLayout.LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT,
        )


        // Erstelle ein weiteres RelativeLayout umd die Position zu definieren
        rl = RelativeLayout(mContext)
        rl.gravity = Gravity.CENTER
        rl.addView(errorTextView)
//        rl.setBackgr
        // Ver-linke die Relative-Layout's miteinander
        layout.addView(rl, params)

        // Schalte den ProgressBar auf unsichtbar
        hide()
    }
}