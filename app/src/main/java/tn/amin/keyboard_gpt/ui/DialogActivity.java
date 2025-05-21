package tn.amin.keyboard_gpt.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import tn.amin.keyboard_gpt.R;
import tn.amin.keyboard_gpt.UiInteracter;
import tn.amin.keyboard_gpt.instruction.InstructionCategory;
import tn.amin.keyboard_gpt.instruction.command.AbstractCommand;
import tn.amin.keyboard_gpt.instruction.command.Commands;
import tn.amin.keyboard_gpt.instruction.command.GenerativeAICommand;
import tn.amin.keyboard_gpt.instruction.command.SimpleGenerativeAICommand;
import tn.amin.keyboard_gpt.language_model.LanguageModel;

public class DialogActivity extends Activity {
    private DialogType mLastDialogType = null;

    private Bundle mLanguageModelsConfig;

    private LanguageModel mSelectedModel;

    private ArrayList<GenerativeAICommand> mCommands;

    private int mCommandIndex = -2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String dialogTypeStr = getIntent().getStringExtra(UiInteracter.EXTRA_DIALOG_TYPE);
        if (dialogTypeStr == null) {
            finish();
            return;
        }

        DialogType dialogType = DialogType.valueOf(dialogTypeStr);
        Dialog dialog = null;

        switch (dialogType) {
            case ChoseModel:
                dialog = buildChoseModelDialog();
                break;
            case WebSearch:
                dialog = buildWebSearchDialog();
                break;
            case EditCommandsList:
                dialog = buildCommandsListDialog();
                break;
            case EditCommand:
                dialog = buildEditCommandDialog();
                break;
            case InstructionPrefix:
                dialog = buildInstructionPrefixDialog();
                break;
        }

        showDialog(dialog, dialogType);
    }

    private Dialog buildInstructionPrefixDialog() {
        LinearLayout layout = (LinearLayout)
                getLayoutInflater().inflate(R.layout.dialog_instruction_prefix, null);

        EditText prefixEditText = layout.findViewById(R.id.edit_prefix);
        String currentPrefix = getIntent().getStringExtra("current_prefix");
        prefixEditText.setText(currentPrefix);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("Instruction Prefix")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newPrefix = prefixEditText.getText().toString();
                    Intent resultIntent = new Intent(UiInteracter.ACTION_DIALOG_RESULT);
                    resultIntent.putExtra("new_prefix", newPrefix);
                    sendBroadcast(resultIntent);
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder.create();
    }

    private Dialog buildCommandsListDialog() {
        ensureHasCommands();

        CharSequence[] names = Stream.concat(Stream.of("New Command"),
                        mCommands.stream().map(AbstractCommand::getCommandPrefix))
                .toArray(CharSequence[]::new);

        return new AlertDialog.Builder(this)
                .setTitle("Select Command")
                .setItems(names, (dialog, which) -> {
                    mCommandIndex = which - 1;

                    Dialog editCommandDialog = buildEditCommandDialog();
                    showDialog(editCommandDialog, DialogType.EditCommand);

                    dialog.dismiss();
                })
                .setOnDismissListener(d -> returnToKeyboard(DialogType.EditCommandsList))
                .create();
    }

    private Dialog buildEditCommandDialog() {
        ensureHasCommands();

        LinearLayout layout = (LinearLayout)
                getLayoutInflater().inflate(R.layout.dialog_command_edit, null);

        EditText prefixEditText = layout.findViewById(R.id.edit_prefix);
        EditText messageEditText = layout.findViewById(R.id.edit_message);

        String title;
        if (mCommandIndex >= 0) {
            GenerativeAICommand command = mCommands.get(mCommandIndex);
            prefixEditText.setText(command.getCommandPrefix());
            messageEditText.setText(command.getTweakMessage());
            title = "Edit " + InstructionCategory.Command.prefix + command.getCommandPrefix();
        }
        else {
            title = "New command";
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setPositiveButton("Ok", (dialog, which) -> {
                    String prefix = prefixEditText.getText().toString().trim();
                    String message = messageEditText.getText().toString();
                    long similarCount = mCommands.stream().filter((c) -> prefix.equals(c.getCommandPrefix())).count();
                    if ((mCommandIndex == -1 && similarCount >= 1)
                            || (mCommandIndex >= 0 && similarCount >= 2)) {
                        Toast.makeText(this, "There is another command with same name", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (mCommandIndex >= 0) {
                        mCommands.remove(mCommandIndex);
                    }
                    else {
                        mCommandIndex = mCommands.size();
                    }

                    mCommands.add(mCommandIndex, new SimpleGenerativeAICommand(prefix, message));

                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    showDialog(buildCommandsListDialog(), DialogType.EditCommandsList);
                    dialog.dismiss();
                })
                .setOnDismissListener(d -> returnToKeyboard(DialogType.EditCommand));

        if (mCommandIndex >= 0) {
            dialogBuilder
                    .setNeutralButton("Delete", (dialog, which) -> {
                        mCommands.remove(mCommandIndex);

                        showDialog(buildCommandsListDialog(), DialogType.EditCommandsList);
                        dialog.dismiss();
                    });
        }

        return dialogBuilder.create();
    }

    private Dialog buildWebSearchDialog() {
        String title = getIntent().getStringExtra(UiInteracter.EXTRA_WEBVIEW_TITLE);
        if (title == null) {
            title = "Untitled";
        }

        String url = getIntent().getStringExtra(UiInteracter.EXTRA_WEBVIEW_URL);
        if (url == null) {
            throw new NullPointerException(UiInteracter.EXTRA_WEBVIEW_URL + " cannot be null");
        }

        WebView webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient()); // Ensures links open in the WebView
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript (optional)
        webView.loadUrl(url); // Replace with your URL
        Dialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(webView)
                .setOnDismissListener(d -> returnToKeyboard(DialogType.WebSearch))
                 .create();
        ColorDrawable back = new ColorDrawable(Color.TRANSPARENT);
        InsetDrawable inset = new InsetDrawable(back, 100, 200, 100, 200);
        dialog.getWindow().setBackgroundDrawable(inset);

        return dialog;
    }

    private Dialog buildConfigureModelDialog() {
        ensureHasReadModelData();

        Bundle modelConfig = mLanguageModelsConfig.getBundle(mSelectedModel.name());
        if (modelConfig == null) {
            throw new RuntimeException("No model " + mSelectedModel.name());
        }

        String subModel = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL);
        subModel = subModel != null ? subModel : mSelectedModel.defaultSubModel;
        String apiKey = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY);
        String baseUrl = modelConfig.getString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL);
        baseUrl = baseUrl != null ? baseUrl : mSelectedModel.defaultBaseUrl;

        LinearLayout layout = (LinearLayout)
                getLayoutInflater().inflate(R.layout.dialog_configue_model, null);

        EditText apiKeyEditText = layout.findViewById(R.id.edit_apikey);
        EditText subModelEditText = layout.findViewById(R.id.edit_model);
        EditText baseUrlEditText = layout.findViewById(R.id.edit_baseurl);

        apiKeyEditText.setText(apiKey);
        subModelEditText.setText(subModel);
        baseUrlEditText.setText(baseUrl);

        // Enable editing for all models
        subModelEditText.setEnabled(true);
        baseUrlEditText.setEnabled(true);

        return new AlertDialog.Builder(this)
                .setTitle(mSelectedModel.label + " configuration")
                .setView(layout)
                .setPositiveButton("Ok", (dialog, which) -> {
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_API_KEY,
                            apiKeyEditText.getText().toString());
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_SUB_MODEL,
                            subModelEditText.getText().toString());
                    modelConfig.putString(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL_BASE_URL,
                            baseUrlEditText.getText().toString());
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    showDialog(buildChoseModelDialog(), DialogType.ChoseModel);
                    dialog.dismiss();
                })
                .setOnDismissListener(d -> returnToKeyboard(DialogType.ConfigureModel))
                .create();
    }

    private Dialog buildChoseModelDialog() {
        ensureHasReadModelData();

        CharSequence[] names = Arrays.stream(LanguageModel.values())
                .map((model) -> model.label).toArray(CharSequence[]::new);

        return new AlertDialog.Builder(this)
                .setTitle("Select Language Model")
                .setSingleChoiceItems(names, mSelectedModel.ordinal(), (dialog, which) -> {
                    mSelectedModel = LanguageModel.values()[which];

                    Dialog apiKeyDialog = buildConfigureModelDialog();
                    showDialog(apiKeyDialog, DialogType.ConfigureModel);

                    dialog.dismiss();
                })
                .setOnDismissListener(d -> returnToKeyboard(DialogType.ChoseModel))
                .create();
    }

    private void returnToKeyboard(DialogType dialogType) {
        Log.d("LSPosed-Bridge", dialogType + " : " + mLanguageModelsConfig);
        if (dialogType == mLastDialogType) {
            Intent broadcastIntent = new Intent(UiInteracter.ACTION_DIALOG_RESULT);

            if (mSelectedModel != null)
                broadcastIntent.putExtra(UiInteracter.EXTRA_CONFIG_SELECTED_MODEL, mSelectedModel.name());
            if (mLanguageModelsConfig != null)
                broadcastIntent.putExtra(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL, mLanguageModelsConfig);
            if (mCommands != null)
                broadcastIntent.putExtra(UiInteracter.EXTRA_COMMAND_LIST, Commands.encodeCommands(mCommands));

            sendBroadcast(broadcastIntent);
            finish();
        }
    }

    private void ensureHasReadModelData() {
        if (mSelectedModel == null)
            mSelectedModel =
                    LanguageModel.valueOf(getIntent().getStringExtra(UiInteracter.EXTRA_CONFIG_SELECTED_MODEL));
        if (mLanguageModelsConfig == null)
            mLanguageModelsConfig =
                    getIntent().getBundleExtra(UiInteracter.EXTRA_CONFIG_LANGUAGE_MODEL);
    }

    private void ensureHasCommands() {
        if (mCommands == null) {
            mCommands = Commands.decodeCommands(
                    getIntent().getStringExtra(UiInteracter.EXTRA_COMMAND_LIST));
        }

        if (mCommandIndex == -2) {
            mCommandIndex = getIntent().getIntExtra(UiInteracter.EXTRA_COMMAND_INDEX, -2);
        }
    }

    private void showDialog(Dialog dialog, DialogType dialogType) {
        mLastDialogType = dialogType;
        if (dialog == null) {
            finish();
            return;
        }

        dialog.setOnDismissListener(d -> finish());
        dialog.show();
    }
}
