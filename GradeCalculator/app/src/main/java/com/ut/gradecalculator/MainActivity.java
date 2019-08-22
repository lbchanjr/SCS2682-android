package com.ut.gradecalculator;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.text.DecimalFormat;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // This TAG will be used for the log message
    private final static String TAG = "GradeCalculator-Main";

    // Declare private variables for the views that will be manipulated in code.
    private Button minButton, avgButton, maxButton;
    private EditText iosEditText, androidEditText, swiftEditText, javaEditText;

    // Set number formatter to display only up to 2 decimal places and suppress zero fractional data.
    DecimalFormat df = new DecimalFormat("#.##");

    // First method called when activity is started.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Indicate that onCreate method is being processed.
        Log.i(TAG, "Processing onCreate...");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup listener for hiding the virtual keyboard when editText view is not the focus.
        setHideKeyboardOnTouch(this, findViewById(R.id.activity_main));

        Log.i(TAG, "Initializing view instance variables...");
        // Set onclick listener for MIN button to this class instance
        minButton = findViewById(R.id.min_button);
        minButton.setOnClickListener(this);

        // Set onclick listener for AVG button to this class instance
        avgButton = findViewById(R.id.avg_button);
        avgButton.setOnClickListener(this);

        // Set onclick listener for MAX button to this class instance
        maxButton = findViewById(R.id.max_button);
        maxButton.setOnClickListener(this);

        // Setup view variables that will later be used by the other methods.
        iosEditText = findViewById(R.id.ios_grade);
        androidEditText = findViewById(R.id.android_grade);
        swiftEditText = findViewById(R.id.swift_grade);
        javaEditText = findViewById(R.id.java_grade);

        // Check if there's a need to restore savedInstanceState data.
        if(savedInstanceState != null) {
            // Load back all the grades that were saved.
            Log.i(TAG, "Loading savedInstanceState data...");
            iosEditText.setText(savedInstanceState.getString("ios"));
            androidEditText.setText(savedInstanceState.getString("android"));
            swiftEditText.setText(savedInstanceState.getString("swift"));
            javaEditText.setText(savedInstanceState.getString("java"));
        }
        // Indicate in log that onCreate processing has finished.
        Log.i(TAG, "Processing onCreate... DONE.");
    }

    // Called before an activity is destroyed to ensure that data inputted by user will not be lost.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);

        // Save the grades before the activity is destroyed.
        Log.i(TAG, "Saving instance variables.");
        savedInstanceState.putString("ios", iosEditText.getText().toString());
        savedInstanceState.putString("android", androidEditText.getText().toString());
        savedInstanceState.putString("swift", swiftEditText.getText().toString());
        savedInstanceState.putString("java", javaEditText.getText().toString());
    }


    // onClick event handler for all the buttons in this activity.
    @Override
    public void onClick(View v) {
        // Process click events by passing the id of the view that clicked
        // so that a report can be generated.
        Log.i(TAG, v.getTag().toString() + " button was clicked");
        generateReport(v.getId());
    }

    // Generates a report for the grades that were inputted depending on the button that was clicked.
    private void generateReport(int id) {

        // Check to see if any of the grades entered needs to be auto-corrected or re-formatted.
        autoCorrectGradeEntries(iosEditText);
        autoCorrectGradeEntries(androidEditText);
        autoCorrectGradeEntries(swiftEditText);
        autoCorrectGradeEntries(javaEditText);

        // Save the grades so that it can be passed to the next report activity that will be started.
        Bundle extras = new Bundle();
        extras.putString("ios", iosEditText.getText().toString());
        extras.putString("android", androidEditText.getText().toString());
        extras.putString("swift", swiftEditText.getText().toString());
        extras.putString("java", javaEditText.getText().toString());

        // Check which button was clicked.
        switch (id) {
            case R.id.min_button:
                // Generate Report with minimum grade.
                extras.putString("result", getMin());
                extras.putString("result_type", "MIN");
                Log.i(TAG, "Generating report for MIN button click");
                break;
            case R.id.avg_button:
                // Generate Report with average grade.
                extras.putString("result", getAverage());
                extras.putString("result_type", "AVG");
                Log.i(TAG, "Generating report for AVG button click");
                break;

            case R.id.max_button:
                // Generate Report with maximum grade.
                extras.putString("result", getMax());
                extras.putString("result_type", "MAX");
                Log.i(TAG, "Generating report for MAX button click");
                break;
        }

        // Start the the report activity by creating and starting the intent.
        Intent intent = new Intent(this, ReportActivity.class);
        intent.putExtras(extras);
        startActivity(intent);

    }

    // Auto-corrects editText entry to ensure that values that will be used for computation of
    // avg, min, max grades will always be a valid number.
    private void autoCorrectGradeEntries(EditText e) {
        // Make sure that all entries in edit text are auto-corrected before generating the report.
        double gradeValue;
        try {
            gradeValue = Double.parseDouble(e.getText().toString());
        } catch (NumberFormatException | NullPointerException nfe) {
            // Set a value of 0 to grades that are not valid numbers.
            gradeValue = 0;
        }

        // Update the editText entry based on the number converted value.
        e.setText(df.format(gradeValue));

    }

    // Returns the numeric equivalent of the grade that was inputted in a specific editText view.
    private double getGrade(int id) {
        // Return numeric equivalent of grades for calculation purposes.
        // Note: Data has already been auto corrected at this point so there's no need
        //       to check for exceptions.
        EditText grade = findViewById(id);

        // Show log to indicate the type of grade being retrieved.
        Log.i(TAG, "Retrieving " + grade.getTag().toString() + " = " +
                grade.getText().toString() + ".");
        return Double.parseDouble(grade.getText().toString());


    }

    // This method returns the average grade for the four grades that were inputted.
    private String getAverage() {
        // Compute and return the average grade by adding the 4 different grades and dividing it by 4.
        double avg_grade = (getGrade(R.id.ios_grade) + getGrade(R.id.android_grade) +
                getGrade(R.id.swift_grade) + getGrade(R.id.java_grade)) / 4;
        Log.i(TAG, "Calculated average grade: " + avg_grade);
        return df.format(avg_grade);

    }

    // This method returns the smallest grade among the four grades that were inputted.
    private String getMin() {
        // Return the minimum grade among the four grades that were inputted.
        double min_grade = Math.min(getGrade(R.id.ios_grade), getGrade(R.id.android_grade));
        min_grade = Math.min(min_grade, getGrade(R.id.swift_grade));
        min_grade = Math.min(min_grade, getGrade(R.id.java_grade));
        Log.i(TAG, "Minimum grade: " + min_grade);
        return df.format(min_grade);
    }


    // This method returns the largest grade among the four grades that were inputted.
    private String getMax() {
        // Return the maximum grade among the four grades that were inputted.
        double max_grade = Math.max(getGrade(R.id.ios_grade), getGrade(R.id.android_grade));
        max_grade = Math.max(max_grade, getGrade(R.id.swift_grade));
        max_grade = Math.max(max_grade, getGrade(R.id.java_grade));
        Log.i(TAG, "Maximum grade: " + max_grade);
        return df.format(max_grade);
    }

    // This method will setup the ontouch listener that will allow the virtual keyboard to be
    // hidden if user loses focus on the editText field.
    public static void setHideKeyboardOnTouch(final MainActivity context, View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        try {
            //Set up touch listener for non-text box views to hide keyboard.
            if (!(view instanceof EditText)) {

                Log.i(TAG, "Hiding virtual keyboard...");
                view.setOnTouchListener(new View.OnTouchListener() {

                    public boolean onTouch(View v, MotionEvent event) {
                        InputMethodManager in = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                        return false;
                    }

                });
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
