package com.example.food_with_tts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
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

    var breakfastInfo : String? = null
    var lunchInfo : String? = null
    var dinnerInfo : String? = null

    var voice : String? = null

    private var resultText : String? = null
    private var textToSpeech : TextToSpeech? = null
    private val REQUEST_CODE_SPEECH_INPUT = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        schoolName.text = intent.getStringExtra("schoolName")
        getfood()

        voiceBtn.setOnClickListener {
            speak()
        }

        //스피너 값 내용
        var voiceList = listOf(0.5f, 1.0f, 1.5f, 2.0f)

        //스피너 값에따라 내용 달라지게 하기
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, voiceList)
        voice_Sp.adapter = adapter
        voice_Sp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedValue = voiceList[position]
                voice = selectedValue.toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
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
                    textToSpeech?.setPitch(voice!!.toFloat())
                    //읽는 속도
                    textToSpeech?.setSpeechRate(0.8f)
                }
            }
        }
    } //onCreate

    private fun getfood(){
        FoodClient.retrofitService2.getTomorrowFood(
                "e40fc13904d84da4a8d398649c324133", "JSON", "1", "100",
                intent.getStringExtra("eduCode")?:"", intent.getStringExtra("schoolCode")?:"", time
        ).enqueue(object : Callback<FoodBase>{
            override fun onResponse(call: Call<FoodBase>, response: Response<FoodBase>) {

                var res = response.body()?.mealServiceDietInfo?.get(1)?.row
                val regex = Regex("[0-9]+.")

                breakfastInfo = regex.replace(res?.get(0)?.DDISH_NM ?: return, "")
                        .replace("<br/>", "\n")
                lunchInfo = regex.replace(res?.get(1)?.DDISH_NM ?: return, "")
                        .replace("<br/>", "\n")
                dinnerInfo = regex.replace(res?.get(2)?.DDISH_NM ?: return, "")
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
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "무엇을 도와드릴까요?")

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
        if(textView.text.contains("오늘 급식")){
            resultText = "아침 점심 저녁중 어느 급식을 알려드릴까요?"
        } else if(textView.text.contains("아침 급식")){

            if(breakfastInfo == null){
                breakfastInfo = "아침급식이 없는날"
            }
            resultText = breakfastInfo+"입니다."
            Log.d("Logd", "breakfast : ${breakfastInfo}")

        } else if(textView.text.contains("점심 급식")){

            if(lunchInfo == null){
                lunchInfo = "점심급식이 없는날"
            }
            resultText = lunchInfo+"입니다."
            Log.d("Logd", "lunch : ${lunchInfo}")

        } else if(textView.text.contains("저녁 급식")){

            if(dinnerInfo == null){
                dinnerInfo = "저녁급식이 없는날"
            }
            resultText = dinnerInfo+"입니다."
            Log.d("Logd", "dinner : ${dinnerInfo}")
        } else{
            resultText = "다시 한번 말씀해 주십시오"
            Log.d("Logd", "no message")
        }

    }

    private fun Speech() {  //TTS
        val text = resultText
        Log.d("Logd", "TTS")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)// API 20
    }
}