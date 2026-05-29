package com.parkmate.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LanguageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language);

        findViewById(R.id.tvBack).setOnClickListener(v -> finish());

        String[] languages = {"Arabic", "English", "Spanish", "French", "Hindi", "Urdu", "German", "Swedish", "Polish", "Danish", "Afghani", "Mandarian", "Chinese", "Japanese", "Korean"};
        String[] codes = {"ar", "en", "es", "fr", "hi", "ur", "de", "sv", "pl", "da", "ps", "zh", "zh", "ja", "ko"};
        
        ListView lv = findViewById(R.id.lvLanguages);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, languages);
        lv.setAdapter(adapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        String currentLang = getSharedPreferences("prefs", MODE_PRIVATE).getString("Locale.Helper.Selected.Language", "en");
        for (int i = 0; i < codes.length; i++) {
            if (codes[i].equals(currentLang)) {
                lv.setItemChecked(i, true);
                break;
            }
        }

        lv.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCode = codes[position];
            LocaleHelper.setLocale(this, selectedCode);
            Toast.makeText(this, "Language set to " + languages[position], Toast.LENGTH_SHORT).show();
            
            // Restart to apply language change globally
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
