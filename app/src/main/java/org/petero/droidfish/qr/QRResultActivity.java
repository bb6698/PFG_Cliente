package org.petero.droidfish.qr;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;

import org.petero.droidfish.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class QRResultActivity extends Activity implements DialogInterface.OnClickListener {

    private ShareActionProvider mShareActionProvider;
    private String PGNText = "";
    private ImageView qrView;
    private ProgressBar qrProgressBar;
    private MenuItem selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrresult);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.result_qr_menu, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);
        selectedItem = item;
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) item.getActionProvider();

        Intent intent = getIntent();
        PGNText = intent.getStringExtra("pgn_text");

        qrView = (ImageView) findViewById(R.id.qrView);
        qrProgressBar = (ProgressBar) findViewById(R.id.qrProgressBar);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("¿Comprimir el código? Se perderán las etiquetas de la partida")
                .setPositiveButton(R.string.ok_button, this)
                .setNegativeButton(R.string.cancel, this).show();
        // Create the AlertDialog object and return it

        // Return true to display menu
        return true;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    public void saveFile(MenuItem item) {

        ImageView qrView = (ImageView) findViewById(R.id.qrView);
        Bitmap image = null;
        try {
            image = ((BitmapDrawable) qrView.getDrawable()).getBitmap();
        } catch (NullPointerException npe) {

        }
        if (image != null) {
            final Bitmap imageFinal = image;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.create_pgn_file, null);
            final EditText text = (EditText) dialogView.findViewById(R.id.create_pgn_filename);
            builder.setView(dialogView).setTitle("Save QR image as ")
                    .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            final String name = text.getText().toString();
                            new AsyncTask<Bitmap, Void, Void>() {
                                @Override
                                protected Void doInBackground(Bitmap... bitmaps) {
                                    try {
                                        File sdPath = new File(Environment.getExternalStorageDirectory(), "DroidFishQR");
                                        sdPath.mkdirs(); // don't forget to make the directory
                                        FileOutputStream stream = new FileOutputStream(sdPath + "/" + name + ".png"); // overwrites this image every time
                                        bitmaps[0].compress(Bitmap.CompressFormat.PNG, 100, stream);
                                        stream.close();

                                    } catch (IOException e) {

                                    }
                                    return null;
                                }
                            }.execute(imageFinal);
                        }
                    }).setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();


        }

    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        AsyncQRGenerator qrGenerator;
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                System.out.println("Antes:" + PGNText);
                PGNText = QRAlphanumParser.parseToAlphanum(PGNText);
                qrGenerator = new AsyncQRGenerator(qrView, qrProgressBar, selectedItem, mShareActionProvider, this.getApplicationContext());
                qrGenerator.execute(PGNText);
                System.out.println("Despues:" + PGNText);
                break;
            case DialogInterface.BUTTON_NEGATIVE:
                qrGenerator = new AsyncQRGenerator(qrView, qrProgressBar, selectedItem, mShareActionProvider, this.getApplicationContext());
                qrGenerator.execute(PGNText);
                System.out.println("NEGATIV");
                break;
        }
    }
}
