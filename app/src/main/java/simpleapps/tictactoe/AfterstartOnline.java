package simpleapps.tictactoe;

import android.app.Dialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class AfterstartOnline extends AppCompatActivity implements View.OnClickListener {

    static int[][] tracker = new int[3][3];
    static int[][] buttonpressed = new int[3][3];
    boolean easy;
    boolean medium;
    boolean hard;
    boolean impossible;
    Random r = new Random();
    int flag = 0, ax = 10, zero = 1, sensorflag = 0, win = 0, i, game = 1, prevrow, prevcol;
    int summ = 0, ctrflag = 0, night = 0, resetchecker = 1, currentgamedonechecker = 0;
    int score1 = 0, score2 = 0, drawchecker = 0;
    int[] sum = new int[8];
    boolean player1ax;
    boolean selectedsingleplayer;

    ImageView q1, q2, q3, q4, q5, q6, q7, q8, q9;
    List<ImageView> checkerlist = new ArrayList<>();
    TextView p1, p2, turn_tv;
    CharSequence player1 = "Player 1";
    CharSequence player2 = "Player 2";
    MediaPlayer mp;
    String gameId;
    boolean startsWith;
    Boolean dbUpdate = false;
    DatabaseReference gameChild;
    Dialog dialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyTimer();
        sanitize_game_db(Utils.getDatabase(getApplicationContext()), Utils.getMAuth().getUid());
    }

    ValueEventListener gameListener;

    private void sanitize_game_db(
            DatabaseReference database,
            String uid
    ) {
        database.child("matches").child(uid).removeValue();
        database.child("game").child(gameId).removeValue();
        try {
            gameChild.removeEventListener(gameListener);
        } catch (Exception e) {
            Log.d("texts", "sanitize_game_db: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            gameChild.removeEventListener(gameListener);
        } catch (Exception e) {
            Log.d("texts", "sanitize_game_db: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        removeThenAddListener();
    }

    String TAG = "texts";

    private void removeThenAddListener() {
        try {
            gameChild.removeEventListener(gameListener);
        } catch (Exception e) {
            Log.d("texts", "sanitize_game_db: " + e.getLocalizedMessage());
        }
        try {
            gameChild.addValueEventListener(gameListener);
        } catch (Exception e) {
            Log.d("texts", "sanitize_game_db: " + e.getLocalizedMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_afterstart_online);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        q1 = findViewById(R.id.u00);
        q2 = findViewById(R.id.u01);
        q3 = findViewById(R.id.u02);
        q4 = findViewById(R.id.m00);
        q5 = findViewById(R.id.m01);
        q6 = findViewById(R.id.m02);
        q7 = findViewById(R.id.b00);
        q8 = findViewById(R.id.b01);
        q9 = findViewById(R.id.b02);
        turn_tv = findViewById(R.id.turn_tv);
        turn_tv.setVisibility(View.VISIBLE);
        checkerlist.add(q1);
        checkerlist.add(q2);
        checkerlist.add(q3);
        checkerlist.add(q4);
        checkerlist.add(q5);
        checkerlist.add(q6);
        checkerlist.add(q7);
        checkerlist.add(q8);
        checkerlist.add(q9);
        CharSequence[] players = getIntent().getCharSequenceArrayExtra("playersnames");
        player1ax = getIntent().getBooleanExtra("player1ax", true);
        selectedsingleplayer = getIntent().getBooleanExtra("selectedsingleplayer", true);
        gameId = getIntent().getExtras().getString("gameId", null);
        easy = getIntent().getBooleanExtra("easy", false);
        medium = getIntent().getBooleanExtra("medium", false);
        hard = getIntent().getBooleanExtra("hard", false);
        impossible = getIntent().getBooleanExtra("impossible", false);

        mp = MediaPlayer.create(this, R.raw.pencilsound);
        mp.setVolume(0.2F, 0.2F);

        player1 = players[0];
        player2 = players[1];

        startsWith = gameId.startsWith(Utils.getMAuth().getUid());
        if (gameId != null && startsWith) {
            flag = 0;
            enableAll();
            ax = 1;
            zero = 10;
        } else {
            flag = 1;
            disableAll();
            ax = 10;
            zero = 1;
        }

        p1 = findViewById(R.id.playerone);
        p2 = findViewById(R.id.playertwo);

        p1.setText(player1);
        p2.setText(player2);

        Toast.makeText(this, "" + player1 + "'s turn", Toast.LENGTH_SHORT).show();
        DatabaseReference game = Utils.getDatabase(this).child("game");
        if (gameId != null) {
            gameChild = game.child(gameId);
            Log.d("texts", "onCreate: " + gameId);
            gameListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Log.d("texts", "onDataChange: " + snapshot.getValue());
                    if (snapshot.getValue() != null) {
                        if (snapshot.getValue().equals("reset")) {
                            gameChild.removeEventListener(gameListener);
                            dismissDialog(dialog);
                            Toast.makeText(AfterstartOnline.this, "User Left the Game", Toast.LENGTH_SHORT).show();
                            doreset();
                            finish();
                        } else if (snapshot.getValue().equals("again")) {
                            dismissDialog(dialog);
                            playmore();
                        } else {
                            for (DataSnapshot s : snapshot.getChildren()) {
                                String key = s.getKey();
                                String v = s.getValue().toString();
                                Log.d("texts", "onDataChange: " + v + " " + Utils.getMAuth().getCurrentUser().getUid());
                                if (key != null && key.length() == 2 && !v.equals(Utils.getMAuth().getCurrentUser().getUid())) {
                                    Log.d("texts", "onDataChange: " + key + " " + v);
                                    Log.d("texts", "onDataChange: aaaa " + key);
                                    dbUpdate = false;
                                    //Should not Update DB dbUpdate = false
                                    pany(key);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d("texts", "onCancelled: " + error.getDetails());
                }
            };
            gameChild.addValueEventListener(gameListener);
        }
        Utils.AdUtils.showBannerAd(
                this,
                getString(R.string.admobBasicBannerId)
        );

    }

    private void dismissDialog(Dialog dialog) {
        if (dialog != null) {
            try {
                dialog.dismiss();
            } catch (Exception e) {

            }
        }
    }

    private void vib(int i, boolean makeSound) {
        Vibrator myVib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        myVib.vibrate(i);
        if (makeSound && mp != null) {
            mp.start();
        }
    }

    private void updateOnDB(String s) {
        skippable = false;
        if (!dbUpdate) {
            gameChild.child(s).setValue(Utils.getMAuth().getCurrentUser().getUid());
        }
        dbUpdate = false;
    }

    public void pany(String location) {
        location = location.trim();
        String[] split = location.trim().split("", 0);
//        updateOnDB(location);
        skippable = false;
        Log.d("texts", "pany: " + dbUpdate);
        if (dbUpdate) {/*If need to update DB*/
            disableAll();
            try {
                String finalLocation = location;
                gameChild.child(location).setValue(Utils.getMAuth().getCurrentUser().getUid()).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("texts", "onComplete: ");
                    }
                }).addOnSuccessListener(unused -> {
                    Log.d("texts", "onSuccess: ");
                    vib(60, true);
                    /**
                     * This is a weird issue on asus zenfone android 9 Device so needed to add this patch
                     * so that it doesn't outputs wrong value
                     */
                    int patch = 0;
                    if (finalLocation.length() < split.length) {
                        patch = split.length - finalLocation.length();
                    }
                    int xval = Integer.parseInt(split[patch]);
                    int yval = Integer.parseInt(split[1 + patch]);
                    if (win == 0 && buttonpressed[xval][yval] == 0) {
                        if (flag % 2 == 0)
                            tracker[xval][yval] = ax;
                        else
                            tracker[xval][yval] = zero;

                        printBoard();
                        winchecker();
                        cpuplay();
                        flag++;
                        if (xval == 0) {
                            buttonpressed[xval][yval]++;
                        } else {
                            ++buttonpressed[xval][yval];
                        }
                    }
                }).addOnFailureListener(e -> Log.d("texts", "onFailure: " + e.getLocalizedMessage()))
                        .addOnCanceledListener(() -> Log.d("texts", "onCanceled: "));
            } catch (Exception e) {
                Log.d("texts", "pany: " + e.getLocalizedMessage());
            }
        } else {
            Log.d("texts", "pany: B");
            vib(60, true);
            /**
             * This is a weird issue on asus zenfone android 9 Device so needed to add this patch
             * so that it doesnt outputs wrong value
             */
            int patch = 0;
            if (location.length() < split.length) {
                patch = split.length - location.length();
            }
            Log.d("texts", "pany: " + location + " " + Arrays.toString(split) + " " + patch);
            int xval = Integer.parseInt(split[patch]);
            Log.d("texts", "pany: a " + xval);
            int yval = Integer.parseInt(split[1 + patch]);
            Log.d("texts", "pany: b " + yval);
            Log.d("texts", "pany: c " + win + " " + buttonpressed[xval][yval] + " " + flag + " " + (flag % 2));
            if (win == 0 && buttonpressed[xval][yval] == 0) {
                if (flag % 2 == 0)
                    tracker[xval][yval] = ax;
                else
                    tracker[xval][yval] = zero;
                printBoard();
                winchecker();
                cpuplay();
                flag++;
                if (xval == 0) {
                    buttonpressed[xval][yval]++;
                } else {
                    ++buttonpressed[xval][yval];
                }
            }
        }
        dbUpdate = false;
    }

    public void p00(View view) {
        updateOnDB("00");
        vib(60, true);
        int xval = 0;
        int yval = 0;
        if (win == 0 && buttonpressed[xval][yval] == 0) {
            if (flag % 2 == 0)
                tracker[xval][yval] = ax;
            else
                tracker[xval][yval] = zero;

            printBoard();
            winchecker();
            cpuplay();
            flag++;
            buttonpressed[xval][yval]++;
        }
    }


    public void p01(View view) {
        updateOnDB("01");

        vib(60, true);

        if (win == 0 && buttonpressed[0][1] == 0) {
            if (flag % 2 == 0) tracker[0][1] = ax;
            else tracker[0][1] = zero;

            printBoard();
            winchecker();
            cpuplay();
            buttonpressed[0][1]++;
            flag++;
        }
    }

    public void p02(View view) {
        updateOnDB("02");
        vib(60, true);
        if (win == 0 && buttonpressed[0][2] == 0) {
            if (flag % 2 == 0) tracker[0][2] = ax;
            else tracker[0][2] = zero;

            printBoard();
            winchecker();
            cpuplay();
            buttonpressed[0][2]++;
            flag++;
        }
    }

    public void p10(View v) {
        updateOnDB("10");
        vib(60, true);
        if (win == 0 && buttonpressed[1][0] == 0) {
            if (flag % 2 == 0) tracker[1][0] = ax;
            else tracker[1][0] = zero;

            printBoard();
            winchecker();
            cpuplay();

            ++buttonpressed[1][0];
            flag++;
        }
    }

    public void p11(View v) {
        updateOnDB("11");
        vib(60, true);
        if (win == 0 && buttonpressed[1][1] == 0) {
            if (flag % 2 == 0) tracker[1][1] = ax;
            else tracker[1][1] = zero;
            printBoard();
            winchecker();
            cpuplay();
            ++buttonpressed[1][1];
            flag++;
        }
    }

    public void p12(View v) {
        updateOnDB("12");
        vib(60, true);
        if (win == 0 && buttonpressed[1][2] == 0) {
            if (flag % 2 == 0) tracker[1][2] = ax;
            else tracker[1][2] = zero;

            printBoard();
            winchecker();
            cpuplay();
            ++buttonpressed[1][2];
            flag++;
        }
    }

    public void p20(View v) {
        updateOnDB("20");
        vib(60, true);
        if (win == 0 && buttonpressed[2][0] == 0) {
            if (flag % 2 == 0) tracker[2][0] = ax;
            else tracker[2][0] = zero;

            printBoard();
            winchecker();
            cpuplay();
            ++buttonpressed[2][0];
            flag++;
        }
    }

    public void p21(View v) {
        updateOnDB("21");
        vib(60, true);
        if (win == 0 && buttonpressed[2][1] == 0) {
            if (flag % 2 == 0) tracker[2][1] = ax;
            else tracker[2][1] = zero;
            printBoard();
            winchecker();
            cpuplay();
            ++buttonpressed[2][1];
            flag++;
        }
    }

    public void p22(View v) {
        updateOnDB("22");
        vib(60, true);
        if (win == 0 && buttonpressed[2][2] == 0) {
            if (flag % 2 == 0) tracker[2][2] = ax;
            else tracker[2][2] = zero;

            printBoard();
            winchecker();
            cpuplay();
            ++buttonpressed[2][2];
            flag++;
        }
    }

    public void cpuplay() {
        if ((selectedsingleplayer) && (win == 0)) {
            if (ifcpuwin()) ;
            else if (ifopowin()) ;
            else if (emptycentre()) ;
            else if (emptycorner()) ;
            else emptyany();


            /***  final Handler handler = new Handler();
             Timer t = new Timer();
             t.schedule(new TimerTask() {
             public void run() {
             handler.post(new Runnable() {
             public void run() {

             //add code to be executed after a pause

             }
             });
             }
             }, 80);****/
            printBoard();
            winchecker();

            flag++;
            return;
        }
    }

    public boolean ifcpuwin() {
        if (!easy) {
            for (i = 0; i < 8; i++) {
                if (sum[i] == 2 * zero) {
                    if (i == 0) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[0][x] == 0)
                                tracker[0][x] = zero;
                    }

                    if (i == 1) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[1][x] == 0)
                                tracker[1][x] = zero;
                    }
                    if (i == 2) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[2][x] == 0)
                                tracker[2][x] = zero;
                    }

                    if (i == 3) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[x][0] == 0)
                                tracker[x][0] = zero;
                    }

                    if (i == 4) {

                        for (int x = 0; x < 3; x++)
                            if (tracker[x][1] == 0)
                                tracker[x][1] = zero;
                    }

                    if (i == 5) {

                        for (int x = 0; x < 3; x++)
                            if (tracker[x][2] == 0)
                                tracker[x][2] = zero;
                    }
                    if (i == 6) {

                        for (int y = 0; y < 3; y++)
                            for (int x = 0; x < 3; x++)
                                if (x == y)
                                    if (tracker[x][y] == 0)
                                        tracker[x][y] = zero;
                    }
                    if (i == 7) {
                        if (tracker[0][2] == 0)
                            tracker[0][2] = zero;
                        else if (tracker[1][1] == 0)
                            tracker[1][1] = zero;
                        else tracker[2][0] = zero;

                    }
                    return true;
                }
            }
        }
        return false;
    }

    public boolean ifopowin() {
        if ((!easy) || (!medium)) {

            for (i = 0; i < 8; i++) {
                if (sum[i] == 2 * ax) {
                    if (i == 0) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[0][x] == 0) {
                                tracker[0][x] = zero;
                                buttonpressed[0][x]++;
                            }
                    }

                    if (i == 1) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[1][x] == 0) {
                                tracker[1][x] = zero;
                                buttonpressed[1][x]++;
                            }
                    }
                    if (i == 2) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[2][x] == 0) {
                                tracker[2][x] = zero;
                                buttonpressed[2][x]++;
                            }
                    }

                    if (i == 3) {
                        for (int x = 0; x < 3; x++)
                            if (tracker[x][0] == 0) {
                                tracker[x][0] = zero;
                                buttonpressed[x][0]++;
                            }
                    }

                    if (i == 4) {

                        for (int x = 0; x < 3; x++)
                            if (tracker[x][1] == 0) {
                                tracker[x][1] = zero;
                                buttonpressed[x][1]++;
                            }
                    }

                    if (i == 5) {

                        for (int x = 0; x < 3; x++)
                            if (tracker[x][2] == 0) {
                                tracker[x][2] = zero;
                                buttonpressed[x][2]++;
                            }
                    }
                    if (i == 6) {

                        for (int y = 0; y < 3; y++)
                            for (int x = 0; x < 3; x++)
                                if (x == y)
                                    if (tracker[x][y] == 0) {
                                        tracker[x][y] = zero;
                                        buttonpressed[x][y]++;
                                    }


                    }
                    if (i == 7) {
                        if (tracker[0][2] == 0) {
                            tracker[0][2] = zero;
                            buttonpressed[0][2]++;
                        } else if (tracker[1][1] == 0) {
                            tracker[1][1] = zero;
                            buttonpressed[1][1]++;
                        } else {
                            tracker[2][0] = zero;
                            buttonpressed[2][0]++;
                        }


                    }
                    return true;
                }
            }

        }
        return false;
    }

    public boolean emptycentre() {
        if (impossible || hard) {
            if (tracker[1][1] == 0) {
                tracker[1][1] = zero;
                buttonpressed[1][1]++;
                return true;
            }
        }
        return false;
    }

    public boolean emptycorner() {


        if (hard || impossible)
            if (((tracker[0][0] + tracker[2][2]) == 2 * ax) || ((tracker[0][2] + tracker[2][0]) == 2 * ax)) {
                for (int k = 0; k < 3; k++)
                    for (int j = 0; j < 3; j++)
                        if ((k + j) % 2 == 1) {
                            if (tracker[k][j] == 0)
                                tracker[k][j] = zero;
                            buttonpressed[k][j]++;
                            return true;
                        }
            }


        if (impossible)
            if (sum[6] == zero || sum[7] == zero) {
                if (sum[6] == zero) {
                    if ((sum[0] + sum[3]) > (sum[2] + sum[5])) {
                        tracker[0][0] = zero;
                        buttonpressed[0][0]++;
                    } else {
                        tracker[2][2] = zero;
                        buttonpressed[2][2]++;
                    }
                    return true;
                }

                if (sum[7] == zero) {
                    if ((sum[0] + sum[5]) > (sum[3] + sum[2])) {
                        tracker[0][2] = zero;
                        buttonpressed[0][2]++;
                    } else {
                        tracker[2][0] = zero;
                        buttonpressed[2][0]++;
                    }
                    return true;
                }

            }


        for (int i = 0; i < 3; i++) {
            if (tracker[0][i] == ax) {
                if (tracker[0][0] == 0) {
                    tracker[0][0] = zero;
                    buttonpressed[0][0]++;
                    return true;
                }
                if (tracker[0][2] == 0) {
                    tracker[0][2] = zero;
                    buttonpressed[0][2]++;
                    return true;
                }
            }
        }

        for (int i = 0; i < 3; i++) {

            if (tracker[2][i] == ax) {
                if (tracker[2][0] == 0) {
                    tracker[2][0] = zero;
                    buttonpressed[2][0]++;
                    return true;
                }
                if (tracker[2][2] == 0) {
                    tracker[2][2] = zero;
                    buttonpressed[2][2]++;
                    return true;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            if (tracker[i][0] == ax) {
                if (tracker[0][0] == 0) {
                    tracker[0][0] = zero;
                    buttonpressed[0][0]++;
                    return true;
                }
                if (tracker[2][0] == 0) {
                    tracker[2][0] = zero;
                    buttonpressed[2][0]++;
                    return true;
                }
            }
        }
        for (int i = 0; i < 3; i++) {
            if (tracker[i][2] == ax) {
                if (tracker[0][2] == 0) {
                    tracker[0][2] = zero;
                    buttonpressed[0][2]++;
                    return true;
                }
                if (tracker[2][2] == 0) {
                    tracker[2][2] = zero;
                    buttonpressed[2][2]++;
                    return true;
                }
            }
        }
        return false;

    }

    public void emptyany() {
        if (ctrflag == 0)
            while (true) {
                int x = rand();
                int y = rand();

                if (tracker[x][y] == 0) {
                    tracker[x][y] = zero;
                    buttonpressed[x][y]++;
                    return;

                }
            }

        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                if (tracker[x][y] == 0) {
                    tracker[x][y] = zero;
                    buttonpressed[x][y]++;
                    return;
                }


    }

    public int rand() {
        return r.nextInt(3);
    }

    public void printBoard() {
        Log.d("texts", "printBoard: " + Arrays.deepToString(tracker));

        if (tracker[0][0] == 1) q1.setImageResource(R.drawable.x);
        if (tracker[0][0] == 10) q1.setImageResource(R.drawable.oo);


        if (tracker[0][1] == 1) q2.setImageResource(R.drawable.x);
        if (tracker[0][1] == 10) q2.setImageResource(R.drawable.oo);


        if (tracker[0][2] == 1) q3.setImageResource(R.drawable.x);
        if (tracker[0][2] == 10) q3.setImageResource(R.drawable.oo);


        if (tracker[1][0] == 1) q4.setImageResource(R.drawable.x);
        if (tracker[1][0] == 10) q4.setImageResource(R.drawable.oo);

        if (tracker[1][1] == 1) q5.setImageResource(R.drawable.x);
        if (tracker[1][1] == 10) q5.setImageResource(R.drawable.oo);


        if (tracker[1][2] == 1) q6.setImageResource(R.drawable.x);
        if (tracker[1][2] == 10) q6.setImageResource(R.drawable.oo);

        if (tracker[2][0] == 1) q7.setImageResource(R.drawable.x);
        if (tracker[2][0] == 10) q7.setImageResource(R.drawable.oo);


        if (tracker[2][1] == 1) q8.setImageResource(R.drawable.x);
        if (tracker[2][1] == 10) q8.setImageResource(R.drawable.oo);

        if (tracker[2][2] == 1) q9.setImageResource(R.drawable.x);
        if (tracker[2][2] == 10) q9.setImageResource(R.drawable.oo);

        resetchecker++;
        checkCurrentUser();

    }

    private void clickRandom() {
        for (int i = 0; i < tracker.length; i++) {
            for (int j = 0; j < tracker[0].length; j++) {
                if (tracker[i][j] == 0) {
                    Log.d("texts", "clickRandom: " + i + "" + j);
                    Log.d("texts", "onDataChange: bbbb " + i + "" + j);
                    dbUpdate = true;
                    //Should Update DB dbUpdate = true
                    pany(i + "" + j);
                    return;
                }
            }
        }
    }

    private void checkCurrentUser() {
        Log.d("texts", "checkCurrentUser: ");
        if (gameId.startsWith(Utils.getMAuth().getUid())) {
            if (flag == 0) {
                disableAll();
            } else if (flag % 2 == 0) {
                disableAll();
            } else {
                enableAll();
            }
        } else {
            if (flag == 1) {
                enableAll();
            } else if (flag % 2 == 1) {
                enableAll();
            } else {
                disableAll();
            }
        }
    }

    boolean skippable = true;
    CountDownTimer ctd = null;

    private void skipAfter10Seconds() {
        if (ctd != null) {
            ctd.cancel();
        }
        ctd = new CountDownTimer(10 * 1000, 1000) {
            @Override
            public void onTick(long l) {
                runOnUiThread(() -> {
                    Log.d("texts", "onTick: " + l);
                    if (skippable) {
                        turn_tv.setText("Your Turn " + (l) / 1000 + " Seconds Left");
                    } else {
                        turn_tv.setText("Opponent's Turn " + (l) / 1000 + " Seconds Left");
                    }
                });
            }

            @Override
            public void onFinish() {
                Log.d("texts", "onFinish: " + skippable);
                if (skippable) {
                    ctd = null;
                    clickRandom();
                }
            }
        };
        if (skippable) {
            ctd.start();
        }
    }

    private void disableAll() {
        Log.d("texts", "disableAll: ");
        skippable = false;
        skipAfter10Seconds();
        turn_tv.setText("Opponents Turn");
        for (ImageView imageView : checkerlist) {
            imageView.setEnabled(false);
        }
    }

    private void enableAll() {
        Log.d("texts", "enableAll: ");
        skippable = true;
        skipAfter10Seconds();
        turn_tv.setText("Your Turn");
        for (ImageView imageView : checkerlist) {
            imageView.setEnabled(true);
        }
    }

    public void showDialog(String whoWon, String scoreWon, String whoLose, String scoreLose) {
        destroyTimer();
        vib(500, false);

        dialog = new Dialog(AfterstartOnline.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout);
//        TextView playerOneScore = dialog.findViewById(R.id.player_one_score);
//        TextView playerTwoScore = dialog.findViewById(R.id.player_two_score);
        TextView titleText = dialog.findViewById(R.id.title_text);
        dialog.setCancelable(false);
        try {
            dialog.show();
        } catch (Exception e) {
            Log.d("texts", "showDialog: " + e.getLocalizedMessage());
        }

        titleText.setText(whoWon);
//        playerOneScore.setText(whoWon+" Score -> "+scoreWon);
//        playerTwoScore.setText(whoLose+"Score -> "+scoreLose);

        Button resetButton = dialog.findViewById(R.id.reset_button);
        Button playAgainButton = dialog.findViewById(R.id.play_again_button);
        resetButton.setText("End Game");
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog(dialog);
                showExitDialog();
            }
        });

        playAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog(dialog);
                playmore();
            }
        });
    }

    private void destroyTimer() {
        skippable = false;
        try {
            if (ctd != null) {
                ctd.cancel();
            }
        } catch (Exception e) {

        }
    }

    public void winchecker() {
        ctrflag++;
        sum[0] = tracker[0][0] + tracker[0][1] + tracker[0][2];
        sum[1] = tracker[1][0] + tracker[1][1] + tracker[1][2];
        sum[2] = tracker[2][0] + tracker[2][1] + tracker[2][2];
        sum[3] = tracker[0][0] + tracker[1][0] + tracker[2][0];
        sum[4] = tracker[0][1] + tracker[1][1] + tracker[2][1];
        sum[5] = tracker[0][2] + tracker[1][2] + tracker[2][2];
        sum[6] = tracker[0][0] + tracker[1][1] + tracker[2][2];
        sum[7] = tracker[0][2] + tracker[1][1] + tracker[2][0];


        currentgamedonechecker++;
        resetchecker++;

        for (int i = 0; i < 8; i++)
            if (sum[i] == 3 || sum[i] == 30) {
                win++;
                if ((sum[i] == 3) && (ax == 1)) {
                    Log.d("texts", "winchecker: A PLAYER WON");
                    Log.d("texts", "winchecker: " + startsWith + " " + gameId);
                    if (selectedsingleplayer) {
                        MediaPlayer winSound = MediaPlayer.create(this, R.raw.winsound);
                        winSound.setVolume(0.4F, 0.4F);
                        winSound.start();
                    }
                    score1++;
                    showDialog("" + player1 + " won!", "" + score1, "" + player2, "" + score2);
                    TextView q1 = findViewById(R.id.p1score);
                    q1.setText("" + score1);

                }
                if ((sum[i] == 3) && (zero == 1)) {
                    Log.d("texts", "winchecker: B");
                    Log.d("texts", "winchecker: " + startsWith + " " + gameId);
                    if (startsWith) {
                        score2++;
                        showDialog("" + player2 + " won!", "" + score2, "" + player1, "" + score1);
                        TextView q1 = findViewById(R.id.p2score);
                        q1.setText("" + score2);
                    } else {
                        score1++;
                        showDialog("" + player1 + " won!", "" + score1, "" + player2, "" + score2);
                        TextView q1 = findViewById(R.id.p1score);
                        q1.setText("" + score1);
                    }

                }
                if ((sum[i] == 30) && (ax == 10)) {
                    Log.d("texts", "winchecker: C");
                    Log.d("texts", "winchecker: " + startsWith + " " + gameId);
                    if (startsWith) {
                        score1++;
                        showDialog("" + player1 + " won!", "" + score1, "" + player2, "" + score2);
                        TextView q1 = findViewById(R.id.p1score);
                        q1.setText("" + score1);
                    } else {
                        score2++;
                        showDialog("You won!", "" + score2, "" + player1, "" + score1);
                        TextView q1 = findViewById(R.id.p2score);
                        q1.setText("" + score2);
                    }

                }
                if ((sum[i] == 30) && (zero == 10)) {
                    Log.d("texts", "winchecker: D CPU WON");
                    Log.d("texts", "winchecker: " + startsWith + " " + gameId);
                    if (selectedsingleplayer) {
                        MediaPlayer winSound = MediaPlayer.create(this, R.raw.losesound);
                        winSound.setVolume(0.4F, 0.4F);
                        winSound.start();
                    }
                    if (startsWith) {
                        score2++;
                        showDialog("" + player2 + " won!", "" + score2, "" + player1, "" + score1);
                        TextView q1 = findViewById(R.id.p2score);
                        q1.setText("" + score2);
                    } else {
                        score1++;
                        showDialog("" + player1 + " won!", "" + score1, "" + player2, "" + score2);
                        TextView q1 = findViewById(R.id.p1score);
                        q1.setText("" + score1);
                    }
                }

            }

        if ((ctrflag == 9) && (win == 0)) {
            showDialog("This is a draw !", "" + score1, "" + player2, "" + score2);
            drawchecker++;
        }


    }  //end winchecker()

    private void playmore() {
        if ((drawchecker > 0) || (win > 0)) {
            game++;
            TextView qq = findViewById(R.id.gamenumber);
            qq.setText("" + game);

            for (int i = 0; i < 8; i++)
                sum[i] = 0;

            drawchecker = 0;


            ImageView q1, q2, q3, q4, q5, q6, q7, q8, q9;
            q1 = findViewById(R.id.u00);
            q2 = findViewById(R.id.u01);
            q3 = findViewById(R.id.u02);
            q4 = findViewById(R.id.m00);
            q5 = findViewById(R.id.m01);
            q6 = findViewById(R.id.m02);
            q7 = findViewById(R.id.b00);
            q8 = findViewById(R.id.b01);
            q9 = findViewById(R.id.b02);
            q1.setImageDrawable(null);
            q2.setImageDrawable(null);
            q3.setImageDrawable(null);
            q4.setImageDrawable(null);
            q5.setImageDrawable(null);
            q6.setImageDrawable(null);
            q7.setImageDrawable(null);
            q8.setImageDrawable(null);
            q9.setImageDrawable(null);

            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    buttonpressed[i][j] = 0;

            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    tracker[i][j] = 0;


            if ((game + 1) % 2 == 0)
                Toast.makeText(this, "" + player1 + "'s turn", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "" + player2 + "'s turn", Toast.LENGTH_SHORT).show();

            win = 0;
            summ = 0;
            ctrflag = 0;
            Log.d("texts", "playmore: game - " + game + ", flag - " + flag + " " + ((game + 2) % 2));
            if (gameId != null && startsWith) {
                if (((game + 2) % 2) == 0) {
                    flag = 1;
                    disableAll();
                } else {
                    flag = 0;
                    enableAll();
                }
            } else {
                if (((game + 2) % 2) == 0) {
                    flag = 0;
                    enableAll();
                } else {
                    flag = 1;
                    disableAll();
                }
            }
            currentgamedonechecker = 0;

            if (selectedsingleplayer && (game % 2 == 0))
                cpuplay();
            gameChild.setValue("again");
        }
    }


    public void doreset() {

        TextView qq = findViewById(R.id.gamenumber);
        qq.setText("" + 1);


        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                tracker[i][j] = 0;

        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                buttonpressed[i][j] = 0;

        ImageView q1, q2, q3, q4, q5, q6, q7, q8, q9;

        q1 = findViewById(R.id.u00);
        q2 = findViewById(R.id.u01);
        q3 = findViewById(R.id.u02);
        q4 = findViewById(R.id.m00);
        q5 = findViewById(R.id.m01);
        q6 = findViewById(R.id.m02);
        q7 = findViewById(R.id.b00);
        q8 = findViewById(R.id.b01);
        q9 = findViewById(R.id.b02);
        q1.setImageDrawable(null);
        q2.setImageDrawable(null);
        q3.setImageDrawable(null);
        q4.setImageDrawable(null);
        q5.setImageDrawable(null);
        q6.setImageDrawable(null);
        q7.setImageDrawable(null);
        q8.setImageDrawable(null);
        q9.setImageDrawable(null);


        win = 0;
        drawchecker = 0;
        summ = 0;
        resetchecker = 0;
        ctrflag = 0;
        score1 = 0;
        score2 = 0;
        game = 1;
        currentgamedonechecker = 0;
        if (gameId != null && startsWith) {
            flag = 0;
            enableAll();
            ax = 1;
            zero = 10;
        } else {
            flag = 1;
            disableAll();
            ax = 10;
            zero = 1;
        }
        TextView qqq = findViewById(R.id.p1score);
        qqq.setText("" + score1);
        TextView qqqq = findViewById(R.id.p2score);
        qqqq.setText("" + score2);

//        Toast.makeText(this, "" + player1 + "'s turn", Toast.LENGTH_SHORT).show();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    private void showExitDialog() {
        dialog = new Dialog(AfterstartOnline.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_layout_exit);
        dialog.setCancelable(false);

        dialog.show();

        Button exit = dialog.findViewById(R.id.yes_button);
        final Button dismiss = dialog.findViewById(R.id.no_button);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doreset();
                gameChild.removeEventListener(gameListener);
                gameChild.setValue("reset");
                finish();
            }
        });

        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog(dialog);
            }
        });
    }

    @Override
    public void onBackPressed() {
        showExitDialog();
    }

    @Override
    public void onClick(View view) {
        if (view.getTag() != null) {
            Log.d("texts", "onDataChange: cccc " + view.getTag().toString());
            dbUpdate = true;
            //Should Update DB dbUpdate = true
            pany(view.getTag().toString());
        }
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.exit) {
//            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.putExtra("EXIT", true);
//            doreset();
//            startActivity(intent);
//        }
//
//        if (id == R.id.daynightmode) {
//
//            if (night % 2 == 0) {
//                View view = this.getWindow().getDecorView();
//                view.setBackgroundColor(Color.parseColor("#000000"));
//                item.setTitle("Day Mode");
//            } else {
//                View view = this.getWindow().getDecorView();
//                view.setBackgroundColor(Color.parseColor("#FFFFFF"));
//                item.setTitle("Night Mode");
//            }
//            night++;
//        }
//
//
//        return super.onOptionsItemSelected(item);
//    }
}


