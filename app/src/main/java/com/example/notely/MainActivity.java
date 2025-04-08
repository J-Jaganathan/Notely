package com.example.notely;
import com.example.notely.R;
import android.Manifest;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_AUDIO_PERMISSION_CODE = 1;

    private TextView tvSpeechText;
    private Button btnRecord;
    private Button btnSave;
    private ImageButton btnMenu;
    private DrawerLayout drawerLayout;
    private ListView notesListView;
    private TextView emptyNotesText;

    private SpeechRecognizer speechRecognizer;
    private boolean isRecording = false;
    private String currentFileName = "";
    private StringBuilder currentText = new StringBuilder();

    private List<String> notesList = new ArrayList<>();
    private Map<String, String> notesContent = new HashMap<>();
    private ArrayAdapter<String> notesAdapter;
    private FloatingActionButton fabAddNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        tvSpeechText = findViewById(R.id.tv_speech_text);
        btnRecord = findViewById(R.id.btn_record);
        btnSave = findViewById(R.id.btn_save);
        btnMenu = findViewById(R.id.btn_menu);
        drawerLayout = findViewById(R.id.drawer_layout);
        notesListView = findViewById(R.id.notes_list_view);
        emptyNotesText = findViewById(R.id.empty_notes_text);
        fabAddNote = findViewById(R.id.fab_add_note);
        fabAddNote.setOnClickListener(v -> showNewNoteDialog());
        // Load saved notes
        loadNotes();

        // Setup notes adapter
        notesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notesList);
        notesListView.setAdapter(notesAdapter);

        // Register context menu for notes list
        registerForContextMenu(notesListView);

        // Check if there are any notes
        updateEmptyNotesView();

        // Setup speech recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizer.setRecognitionListener(new VoiceRecognitionListener());

        // Check for permissions
        checkPermission();

        // Setup click listeners
        setupClickListeners();

        // Register for context menu on text view for copy/share
        registerForContextMenu(tvSpeechText);
    }

    private void setupClickListeners() {
        btnRecord.setOnClickListener(v -> toggleRecording());

        btnSave.setOnClickListener(v -> saveNote());

        // Show edit dialog on long click on text
        tvSpeechText.setOnLongClickListener(v -> {
            if (!currentFileName.isEmpty()) {
                openContextMenu(v);
                return true;
            }
            return false;
        });

        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(findViewById(R.id.nav_view)));

        notesListView.setOnItemClickListener((parent, view, position, id) -> {
            // 1. Hide home screen elements
            findViewById(R.id.tv_recent_label).setVisibility(View.GONE); // Hide "Recent:"
            fabAddNote.setVisibility(View.GONE); // Hide plus button

            // 2. Load selected note
            String selectedNote = notesList.get(position);
            currentFileName = selectedNote;
            currentText = new StringBuilder(notesContent.get(selectedNote));
            tvSpeechText.setText(currentText.toString());
            drawerLayout.closeDrawers();

            // 3. Show recording UI
            btnRecord.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.VISIBLE);
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();

        if (v.getId() == R.id.tv_speech_text) {
            // Text view context menu
            inflater.inflate(R.menu.text_context_menu, menu);
        } else if (v.getId() == R.id.notes_list_view) {
            // Notes list context menu
            inflater.inflate(R.menu.notes_context_menu, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        int itemId = item.getItemId();

        if (itemId == R.id.action_share) {
            shareCurrentNote();
            return true;
        } else if (itemId == R.id.action_copy) {
            copyToClipboard();
            return true;
        } else if (itemId == R.id.action_edit) {
            showEditDialog();
            return true;
        } else if (itemId == R.id.action_delete_note) {
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            deleteNote(info.position);
            return true;
        }
        return super.onContextItemSelected(item);
    }

    private void shareCurrentNote() {
        if (currentFileName.isEmpty() || currentText.toString().isEmpty()) {
            Toast.makeText(this, "No note to share", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, currentFileName);
        shareIntent.putExtra(Intent.EXTRA_TEXT, currentText.toString());
        startActivity(Intent.createChooser(shareIntent, "Share note via"));
    }

    private void copyToClipboard() {
        if (currentText.toString().isEmpty()) {
            Toast.makeText(this, "No text to copy", Toast.LENGTH_SHORT).show();
            return;
        }

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Note Text", currentText.toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Text copied", Toast.LENGTH_SHORT).show();
    }

    private void showEditDialog() {
        fabAddNote.setVisibility(View.GONE);
        if (currentFileName.isEmpty()) {
            Toast.makeText(this, "No note selected", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_note, null);
        EditText contentInput = dialogView.findViewById(R.id.et_note_content);
        contentInput.setText(currentText.toString());

        new MaterialAlertDialogBuilder(this)
                .setTitle("Edit Note")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String editedContent = contentInput.getText().toString().trim();
                    currentText = new StringBuilder(editedContent);
                    tvSpeechText.setText(currentText.toString());
                    saveNote();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteNote(int position) {
        String noteToDelete = notesList.get(position);

        new AlertDialog.Builder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete '" + noteToDelete + "'?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    notesList.remove(position);
                    notesContent.remove(noteToDelete);
                    notesAdapter.notifyDataSetChanged();
                    updateEmptyNotesView();
                    saveNotes();

                    // Reset view if deleted current note
                    if (currentFileName.equals(noteToDelete)) {
                        resetToHomeScreen();
                    }

                    Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void toggleRecording() {
        if (!isRecording) {
            startRecording();
            btnRecord.setText("Stop");
            isRecording = true;
            // Hide FAB when recording
            fabAddNote.setVisibility(View.GONE);
        } else {
            stopRecording();
            btnRecord.setText("Record");
            isRecording = false;
        }
    }

    private void startRecording() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        speechRecognizer.startListening(intent);
    }

    private void stopRecording() {
        speechRecognizer.stopListening();
    }

    private void saveNote() {
        if (currentFileName.isEmpty()) {
            Toast.makeText(this, "Please create a file first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentText.toString().isEmpty()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the note
        notesContent.put(currentFileName, currentText.toString());

        // If it's a new note, add it to the list
        if (!notesList.contains(currentFileName)) {
            notesList.add(0, currentFileName); // Add to top as most recent
            notesAdapter.notifyDataSetChanged();
            updateEmptyNotesView();
        }

        // Save to SharedPreferences
        saveNotes();

        Toast.makeText(this, "Note saved successfully", Toast.LENGTH_SHORT).show();
    }

    private void showNewNoteDialog() {
        // Hide "Recent:" label when creating new note
        findViewById(R.id.tv_recent_label).setVisibility(View.GONE);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_new_file, null);
        EditText fileNameInput = dialogView.findViewById(R.id.et_file_name);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Enter name of file")
                .setView(dialogView)
                .setPositiveButton("Proceed", (dialog, which) -> {
                    String fileName = fileNameInput.getText().toString().trim();
                    if (!fileName.isEmpty()) {
                        currentFileName = fileName;
                        currentText = new StringBuilder();
                        tvSpeechText.setText("");

                        // Hide home elements and show recording UI
                        findViewById(R.id.tv_recent_label).setVisibility(View.GONE);
                        fabAddNote.setVisibility(View.GONE);
                        btnRecord.setVisibility(View.VISIBLE);
                        btnSave.setVisibility(View.VISIBLE);
                    } else {
                        Toast.makeText(MainActivity.this, "Please enter a file name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetToHomeScreen() {
        TextView tvRecentLabel = findViewById(R.id.tv_recent_label);

        // Always hide recording UI
        btnRecord.setVisibility(View.GONE);
        btnSave.setVisibility(View.GONE);

        // Always show FAB on home screen
        fabAddNote.setVisibility(View.VISIBLE);

        if (!notesList.isEmpty()) {
            // Only show "Recent:" if we have notes AND we're truly on home screen
            tvRecentLabel.setVisibility(View.VISIBLE);
            tvSpeechText.setText(notesList.get(0));
        } else {
            tvRecentLabel.setVisibility(View.GONE);
            tvSpeechText.setText("Your notes will appear here");
        }
    }

    private void updateEmptyNotesView() {
        if (notesList.isEmpty()) {
            emptyNotesText.setVisibility(View.VISIBLE);
            notesListView.setVisibility(View.GONE);
        } else {
            emptyNotesText.setVisibility(View.GONE);
            notesListView.setVisibility(View.VISIBLE);
        }
    }

    private void saveNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences("VoiceNotes", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Gson gson = new Gson();
        String notesListJson = gson.toJson(notesList);
        String notesContentJson = gson.toJson(notesContent);

        editor.putString("notesList", notesListJson);
        editor.putString("notesContent", notesContentJson);
        editor.apply();
        // After saving, return to home screen
        resetToHomeScreen();
        findViewById(R.id.tv_recent_label).setVisibility(View.GONE);
        resetToHomeScreen();
    }

    private void loadNotes() {
        SharedPreferences sharedPreferences = getSharedPreferences("VoiceNotes", MODE_PRIVATE);
        Gson gson = new Gson();

        String notesListJson = sharedPreferences.getString("notesList", null);
        String notesContentJson = sharedPreferences.getString("notesContent", null);

        if (notesListJson != null) {
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            notesList = gson.fromJson(notesListJson, listType);
        }

        if (notesContentJson != null) {
            Type mapType = new TypeToken<HashMap<String, String>>(){}.getType();
            notesContent = gson.fromJson(notesContentJson, mapType);
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Denied. The app needs microphone access to work.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
    }

    private class VoiceRecognitionListener implements RecognitionListener {
        @Override
        public void onReadyForSpeech(Bundle params) {
            Toast.makeText(MainActivity.this, "Listening...", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {}

        @Override
        public void onRmsChanged(float rmsdB) {}

        @Override
        public void onBufferReceived(byte[] buffer) {}

        @Override
        public void onEndOfSpeech() {}

        @Override
        public void onError(int error) {
            String errorMessage;
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    errorMessage = "Audio recording error";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    errorMessage = "Client side error";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    errorMessage = "Insufficient permissions";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    errorMessage = "Network error";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    errorMessage = "Network timeout";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    errorMessage = "No match found";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    errorMessage = "RecognitionService busy";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    errorMessage = "Server error";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    errorMessage = "No speech input";
                    break;
                default:
                    errorMessage = "Unknown error";
                    break;
            }
            Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            isRecording = false;
            btnRecord.setText("Record");
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (matches != null && !matches.isEmpty()) {
                String text = matches.get(0);
                if (currentText.length() > 0) {
                    currentText.append("\n");  // Add new line for new entry
                }
                currentText.append(text);
                tvSpeechText.setText(currentText.toString());
            }

            // Ready for next recording segment
            isRecording = false;
            btnRecord.setText("Record");
        }

        @Override
        public void onPartialResults(Bundle partialResults) {}

        @Override
        public void onEvent(int eventType, Bundle params) {}
    }
}