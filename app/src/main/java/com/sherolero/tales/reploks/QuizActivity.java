package com.sherolero.tales.reploks;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;


public class QuizActivity extends Activity {

    //constantes
    private static final String TAG = "QuizActivity";

    //membros privados
    private ArrayList<Questions> perguntas;
    private long seed;
    private int perguntaAtual;
    private int respostasCorretas;
    private int recorde;
    private boolean mitinho;
    public SharedPreferences settings;
    public static final String loksPrefs = "LoksPrefs";

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        perguntas = new ArrayList<Questions>();
        settings = getSharedPreferences(loksPrefs, 0);
        recorde = settings.getInt("recorde", 0);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("seed")) {
                seed = savedInstanceState.getLong("seed");
            } else {
                seed = new Random().nextLong();
            }

            if (savedInstanceState.containsKey("mitinho")) {
                mitinho = savedInstanceState.getBoolean("mitinho");
            } else {
                mitinho = false;
            }

            if (savedInstanceState.containsKey("perguntaAtual")) {
                perguntaAtual = savedInstanceState.getInt("perguntaAtual");
            } else {
                perguntaAtual = 0;
            }

            if (savedInstanceState.containsKey("respostasCorretas")) {
                respostasCorretas = savedInstanceState.getInt("respostasCorretas");
            } else {
                respostasCorretas = 0;
            }
        } else {
            //Sem nenhuma instancia salva
            seed = new Random().nextLong();
            perguntaAtual = 0;
            respostasCorretas = 0;
        }

        new LoadQuestionsTask().execute("perguntas");
        //setContentView(R.layout.activity_quiz);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putLong("seed", seed);
        outState.putInt("perguntaAtual", perguntaAtual);
        outState.putInt("respostasCorretas", respostasCorretas);
        outState.putInt("recorde", recorde);
        super.onSaveInstanceState(outState);
    }

    //TODO: Esse trecho de código possui muito codigo dentro do try/catch. Pensar o que pode ficar de fora
    public ArrayList<String> loadQuestions(final String questionFilePath) {
        final ArrayList<String> perguntas = new ArrayList<String>();
        try {
            for (final String fileName : getAssets().list(questionFilePath)) {
                final InputStream input = getAssets().open(questionFilePath + "/" + fileName);
                final BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                //Popula a lista de perguntas
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    //Ignorar comentarios no arquivo das perguntas
                    if (!inputLine.startsWith("//") && !(inputLine.length() == 0)) {
                        perguntas.add(inputLine);
                    }
                }
            }
        } catch (final IOException e) {
            Log.e(TAG, "IOException while reading questions from file.", e);
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.question_reading_exception), Toast.LENGTH_LONG).show();
        }
        return perguntas;
    }

    private class LoadQuestionsTask extends AsyncTask<String, Integer, Void> {

        //TODO: Corrigir gambiarra abaixo carregando o aquivo especificado e nao todos do diretorio
        @Override
        protected Void doInBackground(String... params) {
            final ArrayList<String> questionsTemp = new ArrayList<String>();
            for (String path : params) {
                questionsTemp.addAll(loadQuestions(path));
            }

            //Randomiza a ordem das perguntas
            final Random rand = new Random(seed);
            Collections.shuffle(questionsTemp, rand);

            //TODO: Corrigir código duplicado de forma porca
            int failedParses = 0;
            perguntas = new ArrayList<Questions>();
            for (String s : questionsTemp) {
                try {
                    perguntas.add(Questions.parse(s));
                } catch (final IllegalArgumentException e) {
                    failedParses++;
                    Log.e(TAG, "Unable to parse question: " + s, e);
                }
            }
            if (failedParses > 0) {
                Toast.makeText(getApplicationContext(), String.format(getResources().getQuantityString(R.plurals.question_reading_parse_fail_number, failedParses), failedParses), Toast.LENGTH_LONG).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Se nenhuma pergunta for carregada, fecha a Activity
            if (perguntas.isEmpty()) {
                QuizActivity.this.finish();
            } else {
                displayQuestion(perguntaAtual);
            }
        }

        private void displayQuestion(final int questionID) {
            setContentView(R.layout.quiz);

            //Mostra a pergunta
            final TextView quizQuestion = (TextView) findViewById(R.id.quiz_question);
            quizQuestion.setText(perguntas.get(questionID).getPergunta());

            //Mostra as respostas
            final ListView respostasQuiz = (ListView) findViewById(R.id.quiz_answers);
            // TODO: Use my own TextView for this list to control appearance, make it look like the rest of the app
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(QuizActivity.this, R.layout.quiz_answer_row, perguntas.get(questionID).getRespostas().toArray(new String[0]));
            respostasQuiz.setAdapter(adapter);

            //Implementando OnClickListener para a ListView
            respostasQuiz.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(final AdapterView<?> parent, View view, final int posicao, final long id) {
                    final String respostaSelecionada = ((TextView) view).getText().toString();
                    if (respostaSelecionada.equals(perguntas.get(questionID).getAnswer())) {
                        respostasCorretas++;
                        perguntaAtual++;
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.answer_correct), Toast.LENGTH_SHORT).show();
                        if (respostasCorretas > recorde) {
                            recorde = respostasCorretas;
                        }
                        if (perguntaAtual >= perguntas.size()) {
                            mitinho = true;
                            goToScoreActivity();
                        } else {
                            displayQuestion(perguntaAtual);
                        }
                    } else {
                        goToScoreActivity();
                    }
                }
            });
        }
    }

    private void goToScoreActivity() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("recorde", recorde);
        editor.apply();
        final Intent i = new Intent(QuizActivity.this, ScoreActivity.class);
        i.putExtra("respostasCorretas", respostasCorretas);
        i.putExtra("recorde", recorde);
        i.putExtra("mitinho", mitinho);
        startActivity(i);
        QuizActivity.this.finish();
    }
    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.quiz, menu);
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
}
