package com.example.smartquizz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ScoreboardAdapter extends ArrayAdapter<ScoreboardEntry> {

    private Context context;
    private int resource;
    private List<ScoreboardEntry> entries;

    public ScoreboardAdapter(@NonNull Context context, int resource, @NonNull List<ScoreboardEntry> entries) {
        super(context, resource, entries);
        this.context = context;
        this.resource = resource;
        this.entries = entries;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        ScoreboardEntry entry = entries.get(position);

        TextView usnTextView = convertView.findViewById(R.id.usnTextView);
        TextView percentageTextView = convertView.findViewById(R.id.percentageTextView);

        usnTextView.setText(entry.getUsn());
        percentageTextView.setText(String.format("%d%%", entry.getCorrectAnswers())); // Display percentage only

        return convertView;
    }
}
