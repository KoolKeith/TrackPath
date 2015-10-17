package personal.free.trackpath;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Application settings dialog.
 *
 * Allows modification of
 *  xml output path
 *
 * Created by r-k- on 17/10/15.
 */
public class SettingsDialog extends Dialog implements View.OnClickListener {
    private EditText nameField;
    private String outFolder;

    public String getOutFolder() {
        return outFolder;
    }

    public void setOutFolder(String outFolder) {
        this.outFolder = outFolder;
        nameField.setText(outFolder);
    }

    public SettingsDialog(Context context) {
        super(context);
        setCancelable(false);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getView());
    }

    @Override
    public void onClick(View v) {
        outFolder = nameField.getText().toString();
        TrackerService.setOutputFile(outFolder);
        dismiss();
    }

    private View getView() {
        LinearLayout layout = new LinearLayout(MainActivity.getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(MainActivity.getActivity());
        textView.setText(R.string.outfldLabel);
        textView.setTextSize(16f);
        layout.addView(textView);

        nameField = new EditText(MainActivity.getActivity());
        layout.addView(nameField);


        LinearLayout layout1 = new LinearLayout(MainActivity.getActivity());
        layout1.setOrientation(LinearLayout.HORIZONTAL);
        Button button = new Button(MainActivity.getActivity());
        button.setText(R.string.ok_label);
        button.setOnClickListener(this);
        layout1.addView(button);
        button = new Button(MainActivity.getActivity());
        button.setText(R.string.cancel_label);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        layout1.addView(button);

        layout.addView(layout1);

        return layout;
    }
}
