package com.example.contactlist;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private SimpleCursorAdapter adapter;
    private ListView listView;
    private Button show, save;
    private FloatingActionButton add,shower;
    private EditText name, phone;
    int i=0;
    int REQUEST_CODE=1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.contactsList);

        name = (EditText) findViewById(R.id.name);
        phone = (EditText) findViewById(R.id.phone);

        shower = (FloatingActionButton) findViewById(R.id.faber);
        shower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(i%2==0){
                adapter = new SimpleCursorAdapter(getApplicationContext(),
                        android.R.layout.simple_list_item_2, null,
                        new String[] { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER},
                        new int[] { android.R.id.text1, android.R.id.text2 }, 0);

                listView.setAdapter(adapter);
                i++;
                getSupportLoaderManager().initLoader(0, null, MainActivity.this);
            }else{
                    listView.setAdapter(null);
                i++;
            }}
        });

        add = (FloatingActionButton) findViewById(R.id.fab);

       add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contactName = name.getText().toString();
                String contactPhone = phone.getText().toString();
                addContact(contactName, contactPhone);
                Toast.makeText(getApplicationContext(), "Contact added", Toast.LENGTH_LONG).show();
            }
        });
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS)+
                ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_CONTACTS)
                !=
                PackageManager.PERMISSION_GRANTED){
            //When permission not granted
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.READ_CONTACTS)||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_CONTACTS)
            ){
                //create AlertDialog
                AlertDialog.Builder builder= new AlertDialog.Builder(
                        MainActivity.this
                );
                builder.setTitle("Дайте,пожалуйста,разрешения");
                builder.setMessage("Приложению нужен доступ к памяти и к микрофону");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{
                                        Manifest.permission.READ_CONTACTS,
                                        Manifest.permission.WRITE_CONTACTS
                                },
                                REQUEST_CODE);
                    }
                });
                builder.setNegativeButton("Cancel",null);
                AlertDialog alertDialog=builder.create();
                alertDialog.show();

            }else{
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{
                                Manifest.permission.READ_CONTACTS,
                                Manifest.permission.WRITE_CONTACTS
                        },
                        REQUEST_CODE);

            }

        }else{
            //When permissions are already granted
            Toast.makeText(getApplicationContext(),"Permissions are alredy granted..",Toast.LENGTH_SHORT).show();

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CODE){
            if((grantResults.length>0)&&(grantResults[0]+grantResults[1]+grantResults[2]==PackageManager.PERMISSION_GRANTED)){
                //permissions are granted
                Toast.makeText(getApplicationContext(),"Permissions GRANTED..",Toast.LENGTH_SHORT).show();
            }else{
                // permissions are denied
                Toast.makeText(getApplicationContext(),"Permissions DENIED..",Toast.LENGTH_SHORT).show();;

            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getApplicationContext(), ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new  String[]{
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID},null,null,null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
            adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void addContact(String name, String phone) {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = ops.size();

        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name) // Name of the person
                .build());
        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(
                        ContactsContract.Data.RAW_CONTACT_ID,   rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone) // Number of the person
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build());
        // Type of mobile number

        try {
            ContentProviderResult[] res = getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }


    }

}



