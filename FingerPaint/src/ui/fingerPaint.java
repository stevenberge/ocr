package ui;
import java.io.ByteArrayOutputStream;
import utils.JpegTool;
import utils.TCPThread;
import android.R.color;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
public class fingerPaint extends GraphicsActivity{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mView=new MyView(this);
		setContentView(mView);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xFFFFFFFF);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);
		mPaint.setStrokeWidth(42);
	}
	private Paint       mPaint;
	private MyView mView;
	public class MyView extends View {
		private static final float MINP = 0.25f;
		private static final float MAXP = 0.75f;
		private Bitmap  mBitmap;
		private Canvas  mCanvas;
		private Path    mPath;
		private Paint   mBitmapPaint;
		public MyView(Context c) {
			super(c);
			setBackgroundColor(color.black);
			mBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		}
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(0x0);
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			canvas.drawPath(mPath, mPaint);
		}
		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 4;
		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}
		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
				mX = x;
				mY = y;
			}
		}
		private void touch_up() {
			mPath.lineTo(mX, mY);
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}
		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}
	}
	private static final int SAVE_MENU_ID = Menu.FIRST;
	private static final int SET_MENU_ID = Menu.FIRST + 1;
	private static final int SEND_MENU_ID = Menu.FIRST + 2;
	private static final int ERASE_MENU_ID = Menu.FIRST + 3;
	private static final int CLEAR_MENU_ID = Menu.FIRST + 4;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, SAVE_MENU_ID, 0, "Save").setShortcut('3', 'c');
		menu.add(0, SET_MENU_ID, 0, "Setip").setShortcut('4', 's');
		menu.add(0, SEND_MENU_ID, 0, "Send").setShortcut('5', 'z');
		menu.add(0, ERASE_MENU_ID, 0, "Erase").setShortcut('5', 'z');
		menu.add(0, CLEAR_MENU_ID, 0, "Clear").setShortcut('5', 'z');
		return true;
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}
	//////////////////////
	//澶勭悊鐐瑰嚮鑿滃崟
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		mPaint.setXfermode(null);
		mPaint.setAlpha(0xFF);
		switch (item.getItemId()) {
		case SAVE_MENU_ID:
			Bitmap bit=mView.mBitmap;
			bit=JpegTool.genMiniThumb(bit, 28, 28);
			return true;
		case SET_MENU_ID:
			setIPDialog().show();
			return true;
		case SEND_MENU_ID:
			onSend();
			return true;
		case ERASE_MENU_ID:
			mPaint.setXfermode(new PorterDuffXfermode(
					PorterDuff.Mode.CLEAR));
			return true;
		case CLEAR_MENU_ID:
			Bitmap old=mView.mBitmap;
			mView.mBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
			mView.mCanvas = new Canvas(mView.mBitmap);
			old.recycle();
			mView.invalidate();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	////////////////////
	//鍙戦�
	private int port = 8888;   
	private String ipAdd = "219.223.194.146"; //252" //"10.254.146.245";//"10.7.77.173";//
	private void showAns(String s){
//		 LayoutInflater mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	     final ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(, null, true);
//	     PopupWindow mPopupWindow = new PopupWindow(menuView, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
//	     mPopupWindow.setBackgroundDrawable(new BitmapDrawable()); 
//	     mPopupWindow.setOutsideTouchable(true);
//	     mPopupWindow.setFocusable(true);
		AnswerDialog(s).show();
	}
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.arg1) {
			case 0:
				String s = (String) msg.obj;
				if (s != null) { Log.i("mhandler.handlemessage",s);
					showAns(s);
				}
				//Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
//				Toast toast = Toast.makeText(getApplicationContext(),
//						"recv the answer:"+s, Toast.LENGTH_LONG);
//				toast.setGravity(Gravity.CENTER, 0, 0);
//				LinearLayout toastView = (LinearLayout) toast.getView();
//				toast.show();
				
				break;
			case 1:
				Bitmap b = (Bitmap) msg.obj;
				break;
			case 2:
				String info=(String) msg.obj;
				Toast.makeText(getApplicationContext(), info,Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};
	protected void onSend() {
		Bitmap bitmap=mView.mBitmap;

		Matrix matrix = new Matrix();
		float scale = ((float)28 / 500);
		matrix.postScale(scale, scale);
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, 500, 500, matrix, true);
		Toast toast = Toast.makeText(getApplicationContext(),
				"sending picture size:"+newbmp.getHeight()+":"+newbmp.getWidth(), Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		LinearLayout toastView = (LinearLayout) toast.getView();
		ImageView imageCodeProject = new ImageView(getApplicationContext());
		imageCodeProject.setImageDrawable(new BitmapDrawable(newbmp));
		toastView.addView(imageCodeProject, 0);
		toast.show();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		newbmp.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
		byte[] bits = baos.toByteArray(); 
		TCPThread t = new TCPThread(ipAdd, port, mHandler, bits);
		new Thread(t).start();
	} 
	
	private AlertDialog.Builder AnswerDialog(String s) {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("answer");
		alert.setMessage(""+s);

		// Set an EditText view to get user input 
		//final EditText input = new EditText(this);
		//alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
//				String value = input.getText().toString();
//				ipAdd = value;
			}
		});
		return alert;
	}
	///////////////////////////////
	////璁剧疆鍙戦�IP
	private AlertDialog.Builder setIPDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Setup IP");
		alert.setMessage("Enter the IP Address of the Computation Server");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				ipAdd = value;
			}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// Canceled.
			}
		});
		return alert;
	}
}