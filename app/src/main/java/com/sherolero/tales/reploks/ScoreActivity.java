package com.sherolero.tales.reploks;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;


public class ScoreActivity extends Activity {

    public static final String TAG = "ScoreActivity";

    private int respostasCorretas;
    private int recorde;
    private boolean mitinho;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null && savedInstanceState.containsKey("respostasCorretas") && savedInstanceState.containsKey("recorde")) {
            respostasCorretas = savedInstanceState.getInt("respostasCorretas");
            //recorde = savedInstanceState.getInt("recorde");
        } else if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("respostasCorretas")
                && getIntent().getExtras().containsKey("recorde")
                && getIntent().getExtras().containsKey("mitinho")) {
            respostasCorretas = getIntent().getExtras().getInt("respostasCorretas");
            recorde = getIntent().getExtras().getInt("recorde");
            mitinho = getIntent().getExtras().getBoolean("mitinho");
        } else {
            Log.w(TAG, "Erro ao passar dados para essa Activity");
        }

        setContentView(R.layout.quiz_end_summary);

        final TextView gameStatus = (TextView) findViewById(R.id.txt_end_of_quiz);
        if (mitinho) {
            gameStatus.setText("Parabéns, todas as perguntas foram respondidas. Você é um mitinho da Rep Loks");
        } else {
            gameStatus.setText("Perdeu! Tente novamente!");
        }

        final TextView respostasCorretasText = (TextView) findViewById(R.id.quiz_end_correct_number);
        respostasCorretasText.setText(String.valueOf(respostasCorretas));

        final TextView recordeText = (TextView) findViewById(R.id.quiz_end_recorde_number);
        recordeText.setText(String.valueOf(recorde));
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putInt("respostasCorretas", respostasCorretas);
        //outState.putInt("recorde", recorde);
        super.onSaveInstanceState(outState);
    }
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.score, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }*/

    public void goToMenu(View v) {
        ScoreActivity.this.finish();
    }
}
