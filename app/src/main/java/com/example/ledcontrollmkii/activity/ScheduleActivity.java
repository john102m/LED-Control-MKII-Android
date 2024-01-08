package com.example.ledcontrollmkii.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ledcontrollmkii.MyInterface;
import com.example.ledcontrollmkii.R;
import com.example.ledcontrollmkii.ScheduleEntry;
import com.example.ledcontrollmkii.dbhelper.ScheduleDbHelper;
import com.example.ledcontrollmkii.services.DatabaseService;

import java.util.ArrayList;

public class ScheduleActivity extends AppCompatActivity implements MyInterface {
    private static final String TAG = "ScheduleActivity";
    private ScheduleDbHelper _scheduleDbHelper = new ScheduleDbHelper(this);
    private DatabaseService _dbService;
    private int _selectedTextViewId = 0;
    private int _selectedSpinnerId = 0;
    private int _selectedRowId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);

        _dbService = new DatabaseService(_scheduleDbHelper);

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });

        setSpinners();

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ArrayList<ScheduleEntry> schedules = _dbService.getSixEntries();
                LinearLayout rootLayout = findViewById(R.id.rootLayout);

                // go through each currently saved schedule and update accordingly
                // this will not work if there were no schedules already saved or
                // if there's less than 5
                schedules.forEach((s) -> {
                    Spinner spinner = rootLayout.findViewWithTag("spinner" + s.getScheduleRow());
                    String modeText = (String) spinner.getSelectedItem();

                    TextView textView = rootLayout.findViewWithTag("textView" + s.getScheduleRow());
                    String selectedTime = (String) textView.getText();

                    _dbService.updateOrInsert(s.getScheduleRow(), modeText, selectedTime);
                });


                //Toast.makeText(ScheduleActivity.this, "Saved", Toast.LENGTH_SHORT).show();
                // Create an Intent to go back to the main activity
                Intent intent = new Intent(ScheduleActivity.this, MainActivity.class);
                intent.putExtra("source","schedule");
                // If you want to pass data back to the main activity, you can use extras
                // intent.putExtra("key", "value");
                // Start the main activity

                //to avoid hitting the onCreate method in MainActivity
                // android:launchMode="singleTask"  is in the manifest
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                // Finish the current activity (optional)
                finish();
            }
        });
    }

    private void setSpinners() {
        ArrayList<ScheduleEntry> schedules = _dbService.getSixEntries();
        Log.i(TAG, "setSpinners: " + schedules.size());
        // Assuming you're in an Activity or Fragment
        Spinner spinner1 = findViewById(R.id.spinner1);
        Spinner spinner2 = findViewById(R.id.spinner2);
        Spinner spinner3 = findViewById(R.id.spinner3);
        Spinner spinner4 = findViewById(R.id.spinner4);
        Spinner spinner5 = findViewById(R.id.spinner5);
        Spinner spinner6 = findViewById(R.id.spinner6);
// Add more spinners as needed

// Retrieve the array of options from resources
        String[] options = getResources().getStringArray(R.array.spinner_options);
// Create an ArrayAdapter using the array of options
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

// Set the adapter for each Spinner
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);
        spinner3.setAdapter(adapter);
        spinner4.setAdapter(adapter);
        spinner5.setAdapter(adapter);
        spinner6.setAdapter(adapter);

        LinearLayout rootLayout = findViewById(R.id.rootLayout);

        schedules.forEach((s) -> {
            Spinner spinner = rootLayout.findViewWithTag("spinner" + s.getScheduleRow());
            int spinnerPosition = adapter.getPosition(s.getMode());
            spinner.setSelection(spinnerPosition);

            TextView textView = rootLayout.findViewWithTag("textView" + s.getScheduleRow());
            textView.setText(s.getEventTime());
        });

    }

    //ctrl + alt + L to format doc


    public void onClick(View view) {

        LinearLayout layout = (LinearLayout) view.getParent();
        LinearLayout layoutParent = (LinearLayout) layout.getParent();

        // loop through all the layouts until the id matches
        int layoutCount = layoutParent.getChildCount();
        View v = null;
        int index = 0; // index is count of linearLayouts within the parent linearLayout
        for (int i = 0; i < layoutCount; i++) {
            v = layoutParent.getChildAt(i);
            if (v instanceof LinearLayout) {
                if (v.getId() == layout.getId()) {
                    _selectedRowId = index;
                    break;
                }
                index++;
            }
        }

        int count = layout.getChildCount();
        v = null;
        // there are only three views - loop through 'til find the
        // one which is a textview and grab its Id
        for (int i = 0; i < count; i++) {
            v = layout.getChildAt(i);
            if (v instanceof Spinner) {
                _selectedSpinnerId = v.getId();

            }
            if (v instanceof TextView) {
                _selectedTextViewId = v.getId();
                Log.i(TAG, "textView: " + v.getId());
                break;
            }
        }

        //send the time from the textView through to the timepicker fragment
        TextView textView = (TextView) findViewById(_selectedTextViewId);
        Bundle args = new Bundle();
        String currentTime = textView.getText().toString();

        args.putInt("currentHour", Integer.valueOf(currentTime.substring(0, 2)));
        args.putInt("currentMinute", Integer.valueOf(currentTime.substring(3)));

        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setArguments(args);
        timePickerFragment.show(this.getSupportFragmentManager(), "time picker");

        Log.i(TAG, "onClick: " + timePickerFragment.getId());
    }

    public void onTimeSet(int hourOfDay, int minute) {
        // Handle the time set event here
        String selectedTime = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
        Log.i(TAG, "onTimeSet: " + selectedTime);

        TextView textView = (TextView) findViewById(_selectedTextViewId);
        textView.setText(selectedTime);

        Spinner spinner = (Spinner) findViewById(_selectedSpinnerId);
        String modeText = spinner.getSelectedItem().toString();

        // Use the selected time as needed
        //String message = "Selected Time: " + selectedTime;
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        //_dbService.insert(String.valueOf(_selectedRowId), modeText, selectedTime);

        //this will allow to create individual DB entries per ROW
        _dbService.updateOrInsert(String.valueOf(_selectedRowId), modeText, selectedTime);

        //ScheduleEntry entry = _dbService.getEntryByRowId(String.valueOf(_selectedRowId));
    }
}