package io.github.project_travel_mate.utilities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Objects;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.project_travel_mate.R;

import static utils.Constants.QR_CODE_HEIGHT;
import static utils.Constants.QR_CODE_WIDTH;
import static utils.Constants.USER_EMAIL;
import static utils.Constants.USER_NAME;

public class ShareContactActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int ACTIVITY_INSERT_CONTACT = 2;
    @BindView(R.id.scan)
    Button scan;
    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_contact);

        ButterKnife.bind(this);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        createCode();
        scan.setOnClickListener(this);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if QRcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if QRCode contains data
                //retrieve results
                String resultContents = result.getContents();
                String[] values = resultContents.split(":");
                String userName = values[0];
                String userEmail = values[1];
                addContact(userName, userEmail);
            }

        }
    }

    private void addContact(String name, String email) {

        Intent contactIntent = new Intent(ContactsContract.Intents.Insert.ACTION);
        contactIntent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        contactIntent
                .putExtra(ContactsContract.Intents.Insert.NAME, name)
                .putExtra(ContactsContract.Intents.Insert.EMAIL, email);

        startActivityForResult(contactIntent, ACTIVITY_INSERT_CONTACT);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.scan:
                IntentIntegrator qrScan = new IntentIntegrator(this);
                qrScan.initiateScan();
                break;
        }
    }

    public void createCode() {

        ImageView qrCodeView = findViewById(R.id.im);
        //getting details to be encoded in qr code
        String myEmail = mSharedPreferences.getString(USER_EMAIL, null);
        String myName = mSharedPreferences.getString(USER_NAME, null);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(myName + " : " + myEmail,
                    BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            //Creating bitmap for generated 2D matrix
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            //saving the generated qr code in device
            String path = Environment.getExternalStorageDirectory().getPath();
            File qrCodeFile = new File(path + "/TravelMate/QRCodes");
            qrCodeFile.mkdir();
            //for providing name to image
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);

            String fname = "Image-" + n + ".jpg";
            File file = new File(qrCodeFile, fname);

            if (file.exists())
                file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            //displaying QRCode on screen
            qrCodeView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, ShareContactActivity.class);
        return intent;
    }
}
