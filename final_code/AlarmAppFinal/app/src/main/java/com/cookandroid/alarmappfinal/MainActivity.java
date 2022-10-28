//1강 안드로이드 스튜디오 설치와 AVD 부팅 확인
package com.cookandroid.alarmappfinal;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Button;
import android.widget.Toast;
import java.util.Random;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    TimePicker alarmTime;//알람 설정 시간을 받아오는 역할.
    TextClock currentTime;//현재 시간.
    TextView timeSet;//알람이 설정된 시간을 저장 및 명시적으로 보여주는 역할.
    Button btn;//알람 설정 버튼.
    Button stopBtn;//알람 멈춤 버튼.
    String randomWord; //
    String inputWord;// PopupActivity에서 음성인식으로 가져온 단어.
    boolean isTesting = false;// Popup이 뜨는 동안,알람을 멈추도록 하는 역할.
    boolean isUsing = false;//알람을 설정한 경우에만 알람이 울리도록 하는 역할.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alarmTime = findViewById(R.id.alarmTime);
        currentTime = findViewById(R.id.textClock);//현재 시간
        timeSet = findViewById(R.id.alarmTimeText);//화면에 표시되는 알람 시간
        btn = findViewById(R.id.Btn);//알람 설정 버튼
        stopBtn = findViewById(R.id.stopBtn);//알람 종료 +음성 인식 시작 버튼

        /*알람 설정 버튼 동작 */
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                timeSet.setText(AlarmTime());//설정된 알람 시간 화면에 표시 및 저장
                Toast.makeText(getApplicationContext(),"알람이 설정되었습니다.",Toast.LENGTH_SHORT).show();
                isUsing = true;//알람을 설정 했다는 데이터.
                return false;
            }
        });

        /*알람 소리 (진동) */
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE));
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                //교재 3강 예제 3-3의 if문 사용
                /*수정 사항 존재(isUsing을 조건에 추가) */
                //현재 시각과 설정된 시각이 같고 알람이 설정된 경우 벨이 울린다.
                // isUsing이 없으면,울리는 알람을 껐는데도 단지 현재 시각과 설정된 시각이 같다는 이유로 알람이 멈추지 않는다.(기존에는 isTesting으로 제어한 것을 isUsing으로 제어한다.)
                if (currentTime.getText().toString().equals(timeSet.getText()) && isUsing){
                    //timeSet.getText()는 CharSequence이다.하지만 String과 equals연산 가능하다.
                    if(!isTesting)
                        r.play();
                    else{//음성인식 활성화 시(버튼 클릭)진동 종료
                        r.stop();
                    }
                }else{// 1분이 지나면 진동 자동 종료
                    r.stop();
                }
                /* ////////// */
            }
        }, 0, 1000);// 1초에 한 번 울린다.

        /*알람 종료 및 음성 인식 버튼 동작 */
        //교재 2강 에제 2-3번의 버튼 동작을 위한 setOnClickListener 사용
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(r.isPlaying()){//알람이 울리는 중이면(이 경우만 클릭이 유의미)
                    isTesting = true;// PopupActivity가 뜬 경우, 알람을 잠시 멈춘다.

                    Intent intent = new Intent(MainActivity.this, PopupActivity.class);
                    // PopupActivity로 이동한다.
                    // (PopupActivity에서 finish()메서드로 인해)다시 MainActivity로 돌아왔을 때는 자동으로 바로 밑에 있는 OnActivityResult함수를 수행한다.
                    startActivityForResult(intent, 0);// requestCode매개변수는 OnActivityResult함수에서 안정성 확보를 위해 쓰인다.(크게 신경쓸 필요X)
                }
            }
        });

    }

    // Intent data에 PopupActivity에서 보낸 음성인식 결과가 String으로 담겨서 온다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==0){//위에서 본 안정성 확보.안하면 앱이 중지될 가능성 up.
            inputWord = data.getStringExtra("inputWord");
            randomWord = data.getStringExtra("randomWord");// Python의 딕셔너리처럼 key값을 주면,그에 해당하는 value를 받는다.
            //음성인식 결과가 발음해야하는 단어와 다른 경우.
            if (!inputWord.equals(randomWord)){//PopupActivity에서 받아온 음성인식값과 랜덤값을 비교
                timeSet.setText(addThreeMin(timeSet.getText().toString()));
                // 3분을 추가해 새로 알람을 맞춘다.(timeSet에 시간을 넣어주면 저장 및 표시 기능을 모두 한다는 것을 잊지 말자!)
                //참고: timeSet.getText()는 CharSequence이므로 String으로 바꾼다.
                Toast.makeText(getApplicationContext(),"정확한 발음 실패. 3분 뒤에 알람이 다시 울립니다.",Toast.LENGTH_SHORT).show();
                isTesting=false;//테스트 끝.
            }
            //음성인식 결과가 발음해야하는 단어와 같은 경우.
            //알람 기능이 제 역할을 다 하고 끝난 것이다.
            else{
                timeSet.setText("설정한 알람 시간이 여기에 표시됩니다.");//다시 원래대로 바꾼다.
                Toast.makeText(getApplicationContext(),"정확한 발음 성공. 알람을 종료합니다.",Toast.LENGTH_SHORT).show();
                isTesting = false;//테스트 끝.
                isUsing = false;//알람 기능 끝.
            }
        }
    }

    // 3분을 더해주는 함수이다.
    public static String addThreeMin(String t){
        String noon = t.substring(0, 2);//오전,오후를 담는 String.
        String[] time = t.substring(3).split(":");//':'를 기준으로 나눈다.
        int hour = Integer.parseInt(time[0]);
        int minute = Integer.parseInt(time[1]);
        String minuteString;
        String result;

        if (noon.equals("오후")){
            hour+=12;
        }

        minute+=3;

        if (minute>=60){
            minute-=60;
            hour+=1;
            if (hour>=24){
                hour-=24;
            }
        }

        if (minute<10){
            minuteString = "0";
            minuteString = minuteString.concat(Integer.toString(minute));
        }else{
            minuteString = Integer.toString(minute);
        }

        //오전 12시라는 표현이 좀 애매하다.
        //밑에 있는 AlarmTime에서 이렇게 정의했으니 상관 없을 것 같다.
        if (hour>12){
            hour-=12;

            result = "오후 "+hour+":"+minuteString;
            return result;
        }else{
            result = "오전 "+hour+":"+minuteString;
            return result;
        }

    }

    /*위젯에 표시된 시간을 String으로 리턴 */
    public String AlarmTime(){

        Integer alarmHours = alarmTime.getCurrentHour();
        Integer alarmMinutes = alarmTime.getCurrentMinute();
        String stringAlarmMinutes;

        if (alarmMinutes<10){
            stringAlarmMinutes = "0";
            stringAlarmMinutes = stringAlarmMinutes.concat(alarmMinutes.toString());
        }else{
            stringAlarmMinutes = alarmMinutes.toString();
        }
        String stringAlarmTime;

        if(alarmHours>12){
            alarmHours = alarmHours - 12;

            stringAlarmTime = "오후 ".concat(alarmHours.toString()).concat(":").concat(stringAlarmMinutes);
        }else{
            stringAlarmTime = "오전 ".concat(alarmHours.toString()).concat(":").concat(stringAlarmMinutes);

        }
        return stringAlarmTime;
    }
}