package com.example.food_with_tts

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import com.example.food_with_tts.data.SchoolBase
import com.example.food_with_tts.retrofit.SchoolClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_start.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.util.*

class StartActivity : AppCompatActivity() {

    private var textToSpeech : TextToSpeech? = null
    private val REQUEST_CODE_SPEECH_INPUT = 100

    var eduCode : String? = null
    var schoolCode : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        startBtn.setOnClickListener {
            Speech()
            CoroutineScope(IO).launch {
                delay(3000L)
                speak()
            }
        }

        textToSpeech = TextToSpeech(this) { status ->       //tts 설정
            if (status == TextToSpeech.SUCCESS) {
                Log.d("Logd", "허용1")
                //사용할 언어를 설정
                val result = textToSpeech?.setLanguage(Locale.KOREAN)
                //이상한 언어 씨부리면
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this@StartActivity, "이 언어는 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d("Logd", "음성허용")
                    //음성 톤
                    textToSpeech?.setPitch(1.0f)
                    //읽는 속도
                    textToSpeech?.setSpeechRate(0.8f)
                }
            }
        }
    } //onCreate

    private fun getSchool(){
        SchoolClient.retrofitService.getSchoolInfo("9289a1f6821e456a8c84bdc3c9fbc523", "JSON", "1", "100", schoolInfo_Tv.text.toString().replace(" ", ""))
            .enqueue(object : Callback<SchoolBase>{
                override fun onResponse(call: Call<SchoolBase>, response: Response<SchoolBase>) {
                    Log.d("Logd", "getSchool onResponse")
                    Log.d("Logd", "searched school is : ${schoolInfo_Tv.text}")

                    eduCode = response.body()?.schoolInfo?.get(1)?.row?.get(0)?.ATPT_OFCDC_SC_CODE
                    schoolCode = response.body()?.schoolInfo?.get(1)?.row?.get(0)?.SD_SCHUL_CODE

                    Log.d("Logd", eduCode.toString())
                    Log.d("Logd", schoolCode.toString())
                    startActivity(Intent(this@StartActivity, MainActivity::class.java).apply {
                        putExtra("eduCode",eduCode)
                        putExtra("schoolCode", schoolCode)
                        putExtra("schoolName", schoolInfo_Tv.text)
                    })
                }

                override fun onFailure(call: Call<SchoolBase>, t: Throwable) {
                    Log.d("Logd", t.message.toString())
                }

            })
    }

    private fun speak() {  //STT
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "다니시는 학교가 어디이신가요?")

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
                    schoolInfo_Tv.text = result!![0]
                    getSchool()
                }
            }
        }
    }

    private fun Speech() {  //TTS
        val text = "다니시는 학교가 어딘가요?"
        Log.d("Logd", "TTS")
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)// API 20
    }
}