package edu.pitt.cs.cs1635.cmn26.mobilevoting2;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static MainActivity inst;
    private String admin = "";

    List<Integer> participants;
    String[] categories;
    HashMap<String, String> tallyTable;
    HashMap<String, Integer> categoryTally;
    String currentLogs;

    TextView logs;
    Button b_self;
    Button b_view;
    EditText edit_num;
    boolean valid = true;

    GraphView graph;
    DataPoint[] points;
    int numMembers;
    Spinner typeChooser;
    Button b_setType;
    TextView tv_num;

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b_self = (Button) findViewById(R.id.b_self);
        b_view = (Button) findViewById(R.id.b_view);
        edit_num = (EditText) findViewById(R.id.edit_number);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        logs = (TextView) findViewById(R.id.logs);
        logs.setTextColor(0xFF192857);
        currentLogs = logs.getText().toString();

        tv_num = (TextView) findViewById(R.id.tv_num);
        b_setType = (Button) findViewById(R.id.b_setType);
        typeChooser = (Spinner) findViewById(R.id.type_proj);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(this, R.array.projType, android.R.layout.simple_spinner_item);
        typeChooser.setAdapter(adapter);
        typeChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        categoryTally = new HashMap<String, Integer>();
        categoryTally.put("Artificial Intelligence", 0);
        categoryTally.put("Computer Architecture", 0);
        categoryTally.put("Database Systems", 0);
        categoryTally.put("Graphics", 0);
        categoryTally.put("Networking", 0);
        categoryTally.put("Security", 0);
        categoryTally.put("Theory", 0);
    }

    public void parseSMS(String sender, String body) {
        if(valid) {
                handleVote(sender, body);
        }
    }

    private void acknowledge(String Receiver, String ack) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(Receiver, null, ack, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addParticipants() {

        for(int i = 0; i < numMembers; i++) participants.add(i);

        logs.setText(currentLogs);
    }

    private void handleVote(String sender, String body) {
        if(Integer.parseInt(body) <= participants.size() && Integer.parseInt(body) > 0) {
            if(tallyTable.containsKey(sender)) {
                currentLogs = currentLogs + "\n " + sender + " has already voted. Vote ineffective.";
                logs.setText(currentLogs);
                acknowledge(sender, "Sorry, you have already voted.");
            } else {
                tallyTable.put(sender, body);
                currentLogs = currentLogs + "\n " + sender + " has voted for " + body;
                logs.setText(currentLogs);
                acknowledge(sender, "You have successfully voted for " + body);

                // Timestamp
                long time = System.currentTimeMillis();
                Date d = new Date(time);
                currentLogs = currentLogs + "\n " + d.toString();
                logs.setText(currentLogs);

                // Graph Functionality
                DataPoint temp = points[Integer.parseInt(body)];
                double x = temp.getX();
                double y = temp.getY();
                points[Integer.parseInt(body)] = new DataPoint(x, y + 1);
                BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(points);
                graph.addSeries(series);

                // Trend Functionality
                String cCategory = categories[Integer.parseInt(body) - 1];
                categoryTally.put(cCategory, categoryTally.get(cCategory) + 1);
            }
        } else {
            currentLogs = currentLogs + "\n Participant" + body + "does not exist.";
            logs.setText(currentLogs);
            acknowledge(sender, "Sorry, participant " + body +" does not exist");
        }

    }

    private void initializeTallyTable() {
        tallyTable = new HashMap<String, String>();
        numMembers = Integer.parseInt(edit_num.getText().toString());
        categories = new String[numMembers];

        // For Trend analysis
        graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxX(numMembers);
        graph.getViewport().setMaxY(25);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getGridLabelRenderer().setNumVerticalLabels(6);
        graph.getGridLabelRenderer().setNumHorizontalLabels(numMembers + 1);
        points = new DataPoint[numMembers];
        for(int i = 0; i < numMembers; i++) {
            points[i] = new DataPoint(i, 0);
        }

        BarGraphSeries<DataPoint> series = new BarGraphSeries<DataPoint>(points);
        graph.addSeries(series);

        currentLogs = currentLogs + "\nThe tally table has been initialized with " + numMembers + " members.";
        logs.setText(currentLogs);

        graph.setVisibility(View.VISIBLE);
        edit_num.setVisibility(View.GONE);
        b_self.setVisibility(View.GONE);
        typeChooser.setVisibility(View.VISIBLE);
        b_setType.setVisibility(View.VISIBLE);
        tv_num.setVisibility(View.VISIBLE);

        tv_num = (TextView) findViewById(R.id.tv_num);


    }

    private void createVotingComponent() {
        participants = new ArrayList<Integer>();
        currentLogs = currentLogs + "The mobile voting system has been initialized.";
        logs.setText(currentLogs);

    }

    public void selfAction(View view) {
        if(b_self.getText().equals("Initialize")) {
            createVotingComponent();
            initializeTallyTable();
            addParticipants();
            b_self.setText("End Voting");
        } else if(b_self.getText().equals("End Voting")) {
            b_self.setText("ARE YOU SURE?");
        } else if(b_self.getText().equals("ARE YOU SURE?")) {
            b_self.setText("REALLY SURE?");
        } else if(b_self.getText().equals("REALLY SURE?")) {
            valid = false;
            b_self.setText("Voting ended.");
        }
    }

    public void viewAction(View view) {
        // View tally table

        Iterator iterator = tallyTable.values().iterator();
        int[] votes = new int[numMembers];
        Arrays.fill(votes, 0);
        while(iterator.hasNext()) {
            int vote = Integer.parseInt((String)iterator.next());
            vote--; // ArrayList size() to array
            votes[vote]++;
        }

        for(int i = 0; i < participants.size(); i++) {
            currentLogs = currentLogs + "\nGroup " + (i + 1) + " has " + votes[i] + " votes.";
            logs.setText(currentLogs);
        }

        for(Map.Entry<String, String> entry : tallyTable.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            currentLogs = currentLogs + "\n" + key + " voted for " + val;
        }

        for(Map.Entry<String, Integer> entry : categoryTally.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue().toString();
            currentLogs = currentLogs + "\n" + key + ": " + val + " votes";

        }

        logs.setText(currentLogs);
    }

    public void setAction(View view) {
        categories[Integer.parseInt(tv_num.getText().toString()) - 1] = typeChooser.getSelectedItem().toString();
        int updated = Integer.parseInt(tv_num.getText().toString()) + 1;
        tv_num.setText(Integer.toString(updated));

        if(Integer.parseInt(tv_num.getText().toString()) == numMembers + 1) {
            b_self.setVisibility(View.VISIBLE);
            b_view.setVisibility(View.VISIBLE);
            typeChooser.setVisibility(View.GONE);
            b_setType.setVisibility(View.GONE);
            tv_num.setVisibility(View.GONE);
        }

    }
}
