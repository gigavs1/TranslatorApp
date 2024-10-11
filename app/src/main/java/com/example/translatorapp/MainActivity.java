package com.example.translatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity<button> extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceText;
    private ImageView micIV;
    private MaterialButton translateBtn;
    private TextView translateTV;
    private TextToSpeech textToSpeech;

    String[] fromLanguage = {"From","English","Afrikaans","Arabic","Belarusian",
            "Bulgarian","Bengali","Catalan","Czech","Welsh","Hindi","Urdu","Danish","German",
            "Greek","Esperanto","Spanish","Estonian","Persian","Finnish","French","Irish",
            "Galician","Gujarati","hebrew","Croatian","Haitian","Hungarian","Indonesian",
            "Icelandic","Italian","Japanese","Georgian","Kannada","Korean","Lithuanian",
            "Latvian","Macedonian","Marathi","Malay","Maltese","Dutch","Norwegian",
            "Polish","Portuguese","Romanian","Russian","Slovak","Albanian","Swedish",
            "Swahili","Tamil","Telugu","Thai","Tagalog","Turkish","Ukranian","Vietnamese"};

    String[] toLanguage = {"To","English","Afrikaans","Arabic","Belarusian",
            "Bulgarian","Bengali","Catalan","Czech","Welsh","Hindi","Urdu","Danish","German",
            "Greek","Esperanto","Spanish","Estonian","Persian","Finnish","French","Irish",
            "Galician","Gujarati","hebrew","Croatian","Haitian","Hungarian","Indonesian",
            "Icelandic","Italian","Japanese","Georgian","Kannada","Korean","Lithuanian",
            "Latvian","Macedonian","Marathi","Malay","Maltese","Dutch","Norwegian",
            "Polish","Portuguese","Romanian","Russian","Slovak","Albanian","Swedish",
            "Swahili","Tamil","Telugu","Thai","Tagalog","Turkish","Ukranian","Vietnamese"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromLanguageCode, toLanguageCode = 0;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fromSpinner = findViewById(R.id. idFromSpinner);
        toSpinner = findViewById(R.id.idFToSpinner);
        sourceText = findViewById(R.id.idEditSource);
        micIV = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslation);
        translateTV = findViewById(R.id.idTranslatedTV);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS){
                    int result = textToSpeech.setLanguage(Locale.getDefault());
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(MainActivity.this, "Language Not Supported", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(MainActivity.this, "Text-to-Speech engine is initialized", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(MainActivity.this, "Initialization Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });

        translateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = translateTV.getText().toString();
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        });



        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long I) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long I) {
                toLanguageCode = getLanguageCode(toLanguage[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this, R.layout.spinner_item, toLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);


        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something");

                try {
                    startActivityForResult(intent, REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                translateTV.setVisibility(View.VISIBLE);
                translateTV.setText("");

                if (sourceText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Enter Text to Translate", Toast.LENGTH_SHORT).show();
                }else if (fromLanguageCode == 0){
                    Toast.makeText(MainActivity.this, "Please Select Source Language", Toast.LENGTH_SHORT).show();
                }else if (fromLanguageCode == 0){
                    Toast.makeText(MainActivity.this, "Please Select the Language to be Translated", Toast.LENGTH_SHORT).show();
                }else{
                    translateText(fromLanguageCode, toLanguageCode, sourceText.getText().toString());
                }

            }
        });
    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {

        translateTV.setText("Downloading Language Model, Please Wait...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
               translateTV.setText("Translation..");
               translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                   @Override
                   public void onSuccess(String s) {
                       translateTV.setText(s);
                   }
               }).addOnFailureListener(new OnFailureListener() {
                   @Override
                   public void onFailure(@NonNull Exception e) {
                       Toast.makeText(MainActivity.this, "Failed to Translate", Toast.LENGTH_SHORT).show();
                   }
               });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Failed to Download Language Model. Check Internet Connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PERMISSION_CODE){
            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            sourceText.setText(result.get(0));
        }
    }

    //    String[] fromLanguage = {"From","English","Afrikaans","Arabic","Belarusian",
//            "Bulgarian","Bengali","Catalan","Czech","Welsh","Hindi","Urdu"}
    private int getLanguageCode(String language) {
        int languageCode = 0;
        switch (language){
            case "English":
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bulgarian":
                languageCode = FirebaseTranslateLanguage.BG;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Danish":
                languageCode = FirebaseTranslateLanguage.DA;
                break;
            case "German":
                languageCode = FirebaseTranslateLanguage.DE;
                break;
            case "Greek":
                languageCode = FirebaseTranslateLanguage.EL;
                break;
            case "Esperanto":
                languageCode = FirebaseTranslateLanguage.EO;
                break;
            case "Spanish":
                languageCode = FirebaseTranslateLanguage.ES;
                break;
            case "Estonian":
                languageCode = FirebaseTranslateLanguage.ET;
                break;
            case "Persian":
                languageCode = FirebaseTranslateLanguage.FA;
                break;
            case "Finnish":
                languageCode = FirebaseTranslateLanguage.FI;
                break;
            case "French":
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Irish":
                languageCode = FirebaseTranslateLanguage.GA;
                break;
            case "Galician":
                languageCode = FirebaseTranslateLanguage.GL;
                break;
            case "Gujarati":
                languageCode = FirebaseTranslateLanguage.GU;
                break;
            case "hebrew":
                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "Croatian":
                languageCode = FirebaseTranslateLanguage.HR;
                break;
            case "Haitian":
                languageCode = FirebaseTranslateLanguage.HR;
                break;
            case "Hungarian":
                languageCode = FirebaseTranslateLanguage.HT;
                break;
            case "Indonesian":
                languageCode = FirebaseTranslateLanguage.ID;
                break;
            case "Icelandic":
                languageCode = FirebaseTranslateLanguage.IS;
                break;
            case "Italian":
                languageCode = FirebaseTranslateLanguage.IT;
                break;
            case "Japanese":
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "Georgian":
                languageCode = FirebaseTranslateLanguage.KA;
                break;
            case "Kannada":
                languageCode = FirebaseTranslateLanguage.KN;
                break;
            case "Korean":
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Lithuanian":
                languageCode = FirebaseTranslateLanguage.LT;
                break;
            case "Latvian":
                languageCode = FirebaseTranslateLanguage.LV;
                break;
            case "Macedonian":
                languageCode = FirebaseTranslateLanguage.MK;
                break;
            case "Marathi":
                languageCode = FirebaseTranslateLanguage.MR;
                break;
            case "Malay":
                languageCode = FirebaseTranslateLanguage.MS;
                break;
            case "Maltese":
                languageCode = FirebaseTranslateLanguage.MT;
                break;
            case "Dutch":
                languageCode = FirebaseTranslateLanguage.NL;
                break;
            case "Norwegian":
                languageCode = FirebaseTranslateLanguage.NO;
                break;
            case "Polish":
                languageCode = FirebaseTranslateLanguage.PL;
                break;
            case "Portuguese":
                languageCode = FirebaseTranslateLanguage.PT;
                break;
            case "Romanian":
                languageCode = FirebaseTranslateLanguage.RO;
                break;
            case "Russian":
                languageCode = FirebaseTranslateLanguage.RU;
                break;
            case "Slovak":
                languageCode = FirebaseTranslateLanguage.SK;
                break;
            case "Albanian":
                languageCode = FirebaseTranslateLanguage.SQ;
                break;
            case "Swedish":
                languageCode = FirebaseTranslateLanguage.SV;
                break;
            case "Swahili":
                languageCode = FirebaseTranslateLanguage.SW;
                break;
            case "Tamil":
                languageCode = FirebaseTranslateLanguage.TA;
                break;
            case "Telugu":
                languageCode = FirebaseTranslateLanguage.TE;
                break;
            case "Thai":
                languageCode = FirebaseTranslateLanguage.TH;
                break;
            case "Tagalog":
                languageCode = FirebaseTranslateLanguage.TL;
                break;
            case "Turkish":
                languageCode = FirebaseTranslateLanguage.TR;
                break;
            case "Ukranian":
                languageCode = FirebaseTranslateLanguage.UK;
                break;
            case "Vietnamese":
                languageCode = FirebaseTranslateLanguage.VI;
                break;

            default:
                languageCode = 0;
        }
        return languageCode;
    }

}