package com.cookandroid.alarmappfinal;

import static android.speech.tts.TextToSpeech.ERROR;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class PopupActivity extends Activity {

    Intent intent,data;
    SpeechRecognizer mRecognizer;
    Button sttBtn, ttsBtn;
    final int PERMISSION = 1;
    String inputWord;
    String randomWord;
    TextView randomtxt; // 목표 단어 저장.
    TextView ipttxt; // 실제 말한 단어 저장.
    private TextToSpeech tts;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_activity);



        if ( Build.VERSION.SDK_INT>= 23 ){
            // 퍼미션 체크.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET,
                    Manifest.permission.RECORD_AUDIO},PERMISSION);
        }
        //교재 2강 chapter 3의 위젯 변수 선언과 id 대입 사용
        sttBtn = (Button) findViewById(R.id.sttStart); // 음성 인식 시작 버튼.
        ttsBtn = (Button) findViewById(R.id.ttsStart); // 음성 시작 버튼.

        randomtxt = findViewById(R.id.sttResult);
        ipttxt = findViewById(R.id.iptWord);

        /* 목표 단어를 랜덤하게 가져온다.*/
        randomWord = "";
        String[] randomTxt = getResources().getStringArray(R.array.random_Word);
        Random rand = new Random();
        int n = rand.nextInt(randomTxt.length); //strings.xml에서 랜덤으로 1개의 단어를 꺼내옴.
        randomWord = randomTxt[n]; //randomWord에 랜덤하게 꺼내온 단어 저장.
        randomtxt.setText(randomWord);

        // tts(text-to-speech)
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    tts.setLanguage(Locale.KOREAN); //한국어로 설정.
                }
            }
        });

        intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");

        // 이 버튼("음성 듣기")를 눌렀을 때, 목표 음성이 나온다.
        ttsBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                tts.speak(randomtxt.getText().toString(), TextToSpeech.QUEUE_ADD, null); // randomtxt에 있는 단어 음성으로 출력.
                return false;
            }
        });

        // 음성 인식을 시작한다.
        sttBtn.setOnClickListener(v -> {
            mRecognizer=SpeechRecognizer.createSpeechRecognizer(this);
            mRecognizer.setRecognitionListener(listener);
            mRecognizer.startListening(intent);
        });
    }

    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(getApplicationContext(),"음성인식을 시작합니다.",Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String message;
            // 교재 3강 chapter 2 예제 3-3에 있는 switch문 이용
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }

            Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message,Toast.LENGTH_SHORT).show();
        }
        // 최종적으로 여기에서 끝나고, 다시 (정보를 들고)MainActivity로 돌아간다.
        @Override
        public void onResults(Bundle results) {
            inputWord = ""; // 초기화.
            // 말을 하면 inputWord에 단어 입력.
            ArrayList<String> matches =
                    results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for(int i = 0; i < matches.size() ; i++){
                inputWord += matches.get(i);
            }
            ipttxt.setText(inputWord);

            // 원하는 정보를 Intent data에 담아 MainActivity로 보낸다.
            Intent data = new Intent();
            data.putExtra("inputWord", inputWord);
            setResult(0, data);
            data.putExtra("randomWord" , randomWord);
            setResult(0,data);  // key value로 보낸다.

            finishTest(); // 테스트 종료.
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게.
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }

    public void finishTest() {
        //액티비티(팝업) 닫기
        finish();
    }

}