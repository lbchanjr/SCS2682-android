package com.ut.gradecalculator;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class ReportActivity extends AppCompatActivity {
    private final static String TAG = "GradeCalculator-Report";
    private Bundle bd;

    // Called when the report activity is started.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "Processing onCreate...");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Retrieved the extra information that was passed when the intent was started
        // from the previous activity.
        Log.i(TAG, "Retrieving intent bundled extras.");
        Intent intent = getIntent();
        bd = intent.getExtras();

        // Display grades for the report activity.
        setGrade(bd.getString("ios"), R.id.ios_report);
        setGrade(bd.getString("android"), R.id.android_report);
        setGrade(bd.getString("swift"), R.id.swift_report);
        setGrade(bd.getString("java"), R.id.java_report);

        // Show the label and grade based on the button that was clicked on the previous activity.
        TextView textView = findViewById(R.id.report_result_label);
        textView.setText(bd.getString("result_type"));
        setGrade(bd.getString("result"), R.id.report_result_grade);
        Log.i(TAG, "Processing onCreate... DONE.");
    }

    // Method that will display the grades (including min, max or avg grade) during the report.
    private void setGrade(String name, int id) {
        // Show log on what type of grade is being displayed.
        Log.i(TAG, "Displaying " +
                ((id != R.id.report_result_grade)?findViewById(id).getTag().toString():bd.getString("result_type")) +
                " with a value of " + name + ".");

        // Update the grades on the textView shown on the right side of the report.
        TextView textView = findViewById(id);
        textView.setText(name);
    }

}
