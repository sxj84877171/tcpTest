package com.example.tcptest;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.lenovo.xphone.transfer.ITransfer;
import com.lenovo.xphone.transfer.ITransferListener;
import com.lenovo.xphone.transfer.RemoteDeviceInfo;
import com.lenovo.xphone.transfer.Role;
import com.lenovo.xphone.transfer.tcp.TCPTransfer;

public class MainActivity extends Activity {

	private ITransfer transfer ;
	private RadioButton client ;
	private RadioButton server;
	private EditText ipEditText;
	private EditText portEditText;
	private TextView state ;
	private Button connect; 
	private EditText content;
	private Button send ;
	private Button sendFile ;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		connect = (Button) this.findViewById(R.id.connect);
		client = (RadioButton) this.findViewById(R.id.radioClient);
		server = (RadioButton) this.findViewById(R.id.radioServer);
		ipEditText = (EditText) this.findViewById(R.id.ip);
		portEditText = (EditText) this.findViewById(R.id.port);
		ipEditText = (EditText) this.findViewById(R.id.ip);
		state = (TextView) this.findViewById(R.id.state);
		connect = (Button) this.findViewById(R.id.connect);
		send = (Button) this.findViewById(R.id.send);
		sendFile = (Button) this.findViewById(R.id.send_file);
		content =  (EditText) this.findViewById(R.id.content);
		connect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Role role = Role.Server;
				if(client.isChecked()){
					role = Role.Client;
				}
				if(transfer != null){
					transfer.Disconnect();
				}
				transfer = new TCPTransfer(role);
				String ip = ipEditText.getText().toString();
				int port = Integer.parseInt(portEditText.getText().toString());
				RemoteDeviceInfo info = new RemoteDeviceInfo();
				info.ip = ip ;
				info.port = port ;
				transfer.registerListener(new ITransferListener() {
					
					@Override
					public void onRecv(String message) {
						postMessage(message);
					}
					
					@Override
					public void onError(int errorCode) {
						postMessage("onError");
					}
					
					@Override
					public void onDisconnect(int disconnectReason) {
						postMessage("onDisconnect");
					}
					
					@Override
					public void onConnect(int errorCode) {
						postMessage("connected");
					}

					@Override
					public void onRecv(byte[] message) {
						postMessage(new String(message));
					}
				});
				transfer.connect(info);
			}
		});
		
		
		send.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(transfer != null){
					transfer.send(content.getText().toString());
				}
			}
		});
		
		sendFile.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
			    intent.setType("*/*"); 
			    intent.addCategory(Intent.CATEGORY_OPENABLE);
			 
			    try {
			        startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), 0);
			    } catch (android.content.ActivityNotFoundException ex) {
			        Toast.makeText(MainActivity.this, "Please install a File Manager.",  Toast.LENGTH_SHORT).show();
			    }
			}
		});
	}
	
	
	public void postMessage(final String message){
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				state.setText(message);
			}
		});
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(transfer != null){
			transfer.Disconnect();
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
        case 0:      
        if (resultCode == RESULT_OK) {  
            // Get the Uri of the selected file 
            Uri uri = data.getData();
            String path = getPath(this, uri);
            if(transfer != null){
    			transfer.send(new File(path));
    		}
        }           
        break;
    }
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public static String getPath(Context context, Uri uri) {
		 
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = { "_data" };
            Cursor cursor = null;
 
            try {
                cursor = context.getContentResolver().query(uri, projection,null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        }
 
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
 
        return null;
    }
}
