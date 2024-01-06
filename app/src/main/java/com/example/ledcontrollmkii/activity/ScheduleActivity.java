package com.example.ledcontrollmkii.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ledcontrollmkii.MyInterface;
import com.example.ledcontrollmkii.R;
import com.example.ledcontrollmkii.dbhelper.ScheduleDbHelper;
import com.example.ledcontrollmkii.services.DatabaseService;

public class ScheduleActivity extends AppCompatActivity implements MyInterface {
    private static final String TAG = "ScheduleActivity";
    Button button;

    ScheduleDbHelper _scheduleDbHelper = new ScheduleDbHelper(this);

    DatabaseService _dbService;

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

    }

    private void setSpinners() {

        // Assuming you're in an Activity or Fragment
        Spinner spinner1 = findViewById(R.id.spinner1);
        Spinner spinner2 = findViewById(R.id.spinner2);
        Spinner spinner3 = findViewById(R.id.spinner3);
        Spinner spinner4 = findViewById(R.id.spinner4);
        Spinner spinner5 = findViewById(R.id.spinner5);
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

    }

    //ctrl + alt + L to format doc
    private int _selectedTextViewId = 0;
    private int _selectedSpinnerId = 0;
    private int _selectedItemId = 0;

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
                    _selectedItemId = index;
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

            }
        }

        //send the id of the textView through to the timepicker fragment
//        Button button = (Button) view;
//        Bundle args = new Bundle();
//        args.putInt("row", textViewId);

        TimePickerFragment timePickerFragment = new TimePickerFragment();
//        timePickerFragment.setArguments(args);
        timePickerFragment.show(this.getSupportFragmentManager(), "time picker");

        Log.i(TAG, "onClick: " + timePickerFragment.getId());
    }

    public void onTimeSet(int row, int hourOfDay, int minute) {
        // Handle the time set event here
        String selectedTime = String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute);
        Log.i(TAG, "onTimeSet: " + selectedTime);

        TextView textView = (TextView) findViewById(_selectedTextViewId);
        Log.i(TAG, "textView: " + textView.getText());
        textView.setText(selectedTime);

        Spinner spinner = (Spinner) findViewById(_selectedSpinnerId);
        String modeText = spinner.getSelectedItem().toString();

        // Use the selected time as needed
        //String message = "Selected Time: " + selectedTime;
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        _dbService.insert(String.valueOf(_selectedItemId), modeText, selectedTime);

    }
}