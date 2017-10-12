package edu.orangecoastcollege.cs273.flagquiz;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Flag Quiz";

    private static final int FLAGS_IN_QUIZ = 5;

    private Button[] mButtons = new Button[8];
    private LinearLayout[] mLayouts = new LinearLayout[4];
    private List<Country> mAllCountriesList;  // all the countries loaded from JSON
    private List<Country> mQuizCountriesList; // countries in current quiz (just 10 of them)
    private List<Country> mFilteredCountryList;
    private Country mCorrectCountry; // correct country for the current question
    private int mTotalGuesses; // number of total guesses made
    private int mCorrectGuesses; // number of correct guesses
    private SecureRandom rng; // used to randomize the quiz
    private Handler handler; // used to delay loading next country

    private TextView mQuestionNumberTextView; // shows current question #
    private ImageView mFlagImageView; // displays a flag
    private TextView mAnswerTextView; // displays correct answer

    private String mRegions;
    private int mChoices; //Stores how many choices selected.

    //Keys for preferences.xml
    private static final String CHOICES = "pref_numberOfChoices";
    public static final String REGIONS = "pref_regions";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"On create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Register onSharedPreferencesChangeListener.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);


        mQuizCountriesList = new ArrayList<>(FLAGS_IN_QUIZ);
        rng = new SecureRandom();
        handler = new Handler();

        // TODO: Get references to GUI components (textviews and imageview)
        mQuestionNumberTextView = (TextView) findViewById(R.id.questionNumberTextView);
        mFlagImageView = (ImageView) findViewById(R.id.flagImageView);
        mAnswerTextView = (TextView) findViewById(R.id.answerTextView);

        mLayouts[0] = (LinearLayout) findViewById(R.id.row1LinearLayout);
        mLayouts[1] = (LinearLayout) findViewById(R.id.row2LinearLayout);
        mLayouts[2] = (LinearLayout) findViewById(R.id.row3LinearLayout);
        mLayouts[3] = (LinearLayout) findViewById(R.id.row4LinearLayout);

        // TODO: Put all 4 buttons in the array (mButtons)
        mButtons[0] = (Button) findViewById(R.id.button);
        mButtons[1] = (Button) findViewById(R.id.button2);
        mButtons[2] = (Button) findViewById(R.id.button3);
        mButtons[3] = (Button) findViewById(R.id.button4);
        mButtons[4] = (Button) findViewById(R.id.button5);
        mButtons[5] = (Button) findViewById(R.id.button6);
        mButtons[6] = (Button) findViewById(R.id.button7);
        mButtons[7] = (Button) findViewById(R.id.button8);

        // TODO: Set mQuestionNumberTextView's text to the appropriate strings.xml resource
        mQuestionNumberTextView.setText(getString(R.string.question,1,FLAGS_IN_QUIZ));
        // TODO: Load all the countries from the JSON file using the JSONLoader
        try
        {
            mAllCountriesList = JSONLoader.loadJSONFromAsset(this);
        }
        catch (IOException e)
        {
            Log.e(TAG, "Error printing JSON File",e);
        }

        mRegions = preferences.getString(REGIONS, "All");
        mChoices = Integer.parseInt(preferences.getString(CHOICES, "4"));
        updateRegion();
        //mFilteredCountryList = new ArrayList<>(mAllCountriesList);
        updateChoices();
        // TODO: Call the method resetQuiz() to start the quiz.
        resetQuiz();

    }

    /**
     * Sets up and starts a new quiz.
     */
    public void resetQuiz() {
        Log.i(TAG,"Im in resetQuiz");

        // COMPLETED: Reset the number of correct guesses made
        mCorrectGuesses = 0;
        // COMPLETED: Reset the total number of guesses the user made
        mTotalGuesses = 0;
        // COMPLETED: Clear list of quiz countries (for prior games played)
        mQuizCountriesList.clear();
        // COMPLETED: Randomly add FLAGS_IN_QUIZ (10) countries from the mAllCountriesList into the mQuizCountriesList
        // COMPLETED: Ensure no duplicate countries (e.g. don't add a country if it's already in mQuizCountriesList)
        Country newCountry;
        int size = mFilteredCountryList.size();
        int randomPosition;
        while (mQuizCountriesList.size() < FLAGS_IN_QUIZ) {
            randomPosition = rng.nextInt(size);
            newCountry = mFilteredCountryList.get(randomPosition);
            Log.i(TAG, "Im in resetQuiz loop: " + newCountry.getName());

            if (!mQuizCountriesList.contains(newCountry))
            {
                mQuizCountriesList.add(newCountry);
                Log.i(TAG, "Country Added");
            }
        }

        // COMPLETED: Start the quiz by calling loadNextFlag
        loadNextFlag();
    }

    /**
     * Method initiates the process of loading the next flag for the quiz, showing
     * the flag's image and then 4 buttons, one of which contains the correct answer.
     */
    private void loadNextFlag() {
        // TODO: Initialize the mCorrectCountry by removing the item at position 0 in the mQuizCountries
        mCorrectCountry = mQuizCountriesList.remove(0);
        // TODO: Clear the mAnswerTextView so that it doesn't show text from the previous question
        mAnswerTextView.setText("");
        // TODO: Display current question number in the mQuestionNumberTextView
        int questionNumber = FLAGS_IN_QUIZ - mQuizCountriesList.size();
        mQuestionNumberTextView.setText(getString(R.string.question,questionNumber,FLAGS_IN_QUIZ));
        // TODO: Use AssetManager to load next image from assets folder
        AssetManager am = getAssets();
        try
        {
            InputStream stream = am.open(mCorrectCountry.getFileName());
            Drawable image = Drawable.createFromStream(stream, mCorrectCountry.getName());
            mFlagImageView.setImageDrawable(image);
        }
        catch (IOException e)
        {
            Log.e(TAG,"Error loading the image", e);
        }
        // TODO: Get an InputStream to the asset representing the next flag
        // TODO: and try to use the InputStream to create a Drawable
        // TODO: The file name can be retrieved from the correct country's file name.
        // TODO: Set the image drawable to the correct flag.

        // TODO: Shuffle the order of all the countries (use Collections.shuffle)
        do {
            Collections.shuffle(mFilteredCountryList);
        }
        while (mAllCountriesList.subList(0, mChoices).contains(mCorrectCountry));

        // TODO: Loop through all 4 buttons, enable them all and set them to the first 4 countries
        // TODO: in the all countries list
        for (int i = 0; i < mChoices; i++)
        {
            mButtons[i].setEnabled(true);
            mButtons[i].setText(mFilteredCountryList.get(i).getName());
        }

        // TODO: After the loop, randomly replace one of the 4 buttons with the name of the correct country
        mButtons[rng.nextInt(mChoices)].setText(mCorrectCountry.getName());
    }

    /**
     * Handles the click event of one of the 4 buttons indicating the guess of a country's name
     * to match the flag image displayed.  If the guess is correct, the country's name (in GREEN) will be shown,
     * followed by a slight delay of 2 seconds, then the next flag will be loaded.  Otherwise, the
     * word "Incorrect Guess" will be shown in RED and the button will be disabled.
     * @param v
     */
    public void makeGuess(View v) {
        // TODO: Downcast the View v into a Button (since it's one of the 4 buttons)
        Button clickedButton = (Button) v;
        // TODO: Get the country's name from the text of the button
        String guess = clickedButton.getText().toString();
        // TODO: If the guess matches the correct country's name, increment the number of correct guesses,
        mTotalGuesses++;
        if (guess.equals(mCorrectCountry.getName()))
        {
            //Disable All buttons.
            for (Button b : mButtons)
                b.setEnabled(false);

            mCorrectGuesses++;
            mAnswerTextView.setText(mCorrectCountry.getName());
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.correct_answer));

            if (mCorrectGuesses < FLAGS_IN_QUIZ)
            {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextFlag();
                    }
                }, 2000);
            }
            else
            {
                AlertDialog.Builder builder= new AlertDialog.Builder(this);
                //builder.setMessage(getString(R.string.results,mTotalGuesses, (double) mCorrectGuesses / mTotalGuesses));
                builder.setMessage(getString(R.string.results, mTotalGuesses,(double) mCorrectGuesses / mTotalGuesses));
                builder.setPositiveButton(R.string.reset_quiz, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetQuiz();
                    }
                });
                builder.setCancelable(false);
                builder.create();
                builder.show();
            }
        }
        else
        {
            mAnswerTextView.setText(getString(R.string.incorrect_answer));
            mAnswerTextView.setTextColor(ContextCompat.getColor(this, R.color.incorrect_answer));
            clickedButton.setEnabled(false);
        }


        // TODO: then display correct answer in green text.  Also, disable all 4 buttons (can't keep guessing once it's correct)
        // TODO: Nested in this decision, if the user has completed all 10 questions, show an AlertDialog
        // TODO: with the statistics and an option to Reset Quiz

        // TODO: Else, the answer is incorrect, so display "Incorrect Guess!" in red
        // TODO: and disable just the incorrect button.



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Make intent going to settings activity.
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
        return super.onOptionsItemSelected(item);
    }

    SharedPreferences.OnSharedPreferenceChangeListener mPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //Lets figure out what key changed.

            switch (key)
            {
                case CHOICES:
                    //Read number of choices from shared preferences.
                    mChoices = Integer.parseInt(sharedPreferences.getString(CHOICES, "4"));
                    //call method to update choices.
                    updateChoices();
                    resetQuiz();
                    break;
                case REGIONS:
                    mRegions = sharedPreferences.getString(REGIONS, "All");
                    updateRegion();
                    resetQuiz();
                    break;
            }
            Toast.makeText(MainActivity.this,getString(R.string.restarting_quiz),Toast.LENGTH_SHORT).show();
        }
    };

    private void updateChoices() {
        //Enable all the linear layouts < number of choices / 2.
        //Disable/Hide all the other Linear Layout
        // Lets loop through all linear layouts
        for (int i = 0; i < mLayouts.length; i++)
        {
            if (i < mChoices / 2)
            {
                mLayouts[i].setEnabled(true);
                mLayouts[i].setVisibility(View.VISIBLE);
            }
            else
            {
                mLayouts[i].setEnabled(false);
                mLayouts[i].setVisibility(View.GONE);
            }
        }
    }

    private void updateRegion()
    {
        //Make a decision:
        // If the region is "All, filtered list is the same as all;
        if (mRegions.equals("All"))
            mFilteredCountryList = new ArrayList<>(mAllCountriesList);
        else
        {
            mFilteredCountryList = new ArrayList<>();
            //Loop through all countries.
            for (Country c: mAllCountriesList)
                if (c.getRegion().equals(mRegions))
                    mFilteredCountryList.add(c);
        }
    }
}
