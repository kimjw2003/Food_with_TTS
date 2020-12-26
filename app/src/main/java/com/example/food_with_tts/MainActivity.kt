package com.example.food_with_tts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.example.food_with_tts.data.FoodBase
import com.example.food_with_tts.retrofit.FoodClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val currentTime = Calendar.getInstance().time
    private val time = SimpleDateFormat("YYYYMMdd", Locale.KOREA).format(currentTime)

    var foodInfo : String? = null

    private var resultText : String? = null
    private var textToSpeech : TextToSpeech? = null
    private val REQUEST_CODE_SPEECH_INPUT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getfood()

        voiceBtn.setOnClickListener {
            speak()
        }

        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                Log.d("Logd", "허용1")
                //사용할 언어를 설정
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                //이상한 언어 씨부리면
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this@MainActivity, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("Logd", "음성허용")
                    //음성 톤
                    textToSpeech?.setPitch(1.0f)
                    //읽는 속도
                    textToSpeech?.setSpeechRate(0.6f)
                }
            }
        }
    } //onCreate

    private fun getfood(){
        FoodClient.retrofitService2.getTomorrowFood(
                "e40fc13904d84da4a8d398649c324133", "JSON", "1", "100",
                "D10", "7240393", "" + time
        ).enqueue(object : Callback<FoodBase>{
            override fun onResponse(call: Call<FoodBase>, response: Response<FoodBase>) {

                var res = response.body()?.mealServiceDietInfo?.get(1)?.row?.get(0)
                val regex = Regex("[0-9]+.")

                foodInfo = regex.replace(res?.DDISH_NM ?: return, "")
                        .replace("<br/>", "\n")
            }

            override fun onFailure(call: Call<FoodBase>, t: Throwable) {
                Log.d("Logd", t.message.toString())
            }
        })
    }

    private fun speak() {  //STT
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Hi speak something")

        try{
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        }catch (e : Exception){
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            REQUEST_CODE_SPEECH_INPUT -> {
                if(resultCode == Activity.RESULT_OK && null != data){
                    //음성데이터를 텍스트뷰에 입력
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    textView.text = result!![0]

                    TextInfo()

                    CoroutineScope(Dispatchers.IO).launch {
                        delay(1000L)
                        Speech()
                    }
                }
            }
        }
    }

    private fun TextInfo(){
        if(textView.text == "내일 급식 알려 줘"){
            resultText = "몰라염"
        }
    }

    private fun Speech() {  //TTS
        val text = foodInfo
        Log.d("Logd", "TTS")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)// API 20
    }
}