package net.bible.android.view.activity.base;

import net.bible.android.view.util.UiUtils;
import net.bible.service.history.HistoryManager;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

/** Base class for activities
 * 
 * @author Martin Denham [mjdenham at gmail dot com]
 * @see gnu.lgpl.License for license details.<br>
 *      The copyright to this program is held by it's author.
 */
public class ListActivityBase extends ListActivity implements AndBibleActivity {

	private boolean integrateWithHistoryManager;
	
	private static final String TAG = "ListActivityBase";
	
    public ListActivityBase() {
		super();
	}
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(getLocalClassName(), "onCreate");

        // Register current activity in onCreate and onresume
        CurrentActivityHolder.getInstance().setCurrentActivity(this);

        // fix for null context class loader (http://code.google.com/p/android/issues/detail?id=5697)
        // this affected jsword dynamic classloading
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

        setFullScreen(SharedActivityState.getInstance().isFullScreen());
        
		UiUtils.applyTheme(this);
    }

    @Override
	public void startActivity(Intent intent) {
    	beforeStartActivity();
    	
		super.startActivity(intent);
	}
	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
    	beforeStartActivity();

    	super.startActivityForResult(intent, requestCode);
	}
    /**
     * about to change activity so tell the HistoryManager so it can register the old activity in its list
     */
	protected void beforeStartActivity() {
		if (integrateWithHistoryManager) {
			HistoryManager.getInstance().beforePageChange();
		}
	}

	/**	This will be called automatically for you on 2.0 or later
	 */
	@Override
	public void onBackPressed() {
		if (integrateWithHistoryManager && HistoryManager.getInstance().canGoBack()) {
			Log.d(TAG, "Go back");
			goBack();
		} else {
			super.onBackPressed();
		}
	}
	
	/** called by Android 2.0 +
	 */
	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		// ignore long press on search because it causes errors
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		}

		//TODO make Long press work for screens other than main window e.g. does not work from search screen because wrong window is displayed 
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	    	// just goBack for now rather than displaying History list
	    	goBack();
	    	return true;
	    }
	    
	    return super.onKeyLongPress(keyCode, event);
	}
	
	/** go back to previous screen 
	 */
	protected void goBack() {
		HistoryManager.getInstance().goBack();
	}

	private void setFullScreen(boolean isFullScreen) {
    	if (!isFullScreen) {
    		Log.d(TAG, "NOT Fullscreen");
    		// http://stackoverflow.com/questions/991764/hiding-title-in-a-fullscreen-mode
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    	} else {
    		Log.d(TAG, "Fullscreen");
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    	}
	}

	@SuppressWarnings("unchecked")
	protected void notifyDataSetChanged() {
		ListAdapter listAdapter = getListAdapter();
    	if (listAdapter!=null && listAdapter instanceof ArrayAdapter) {
    		((ArrayAdapter)listAdapter).notifyDataSetChanged();
    	} else {
    		Log.w(TAG, "Could not update list Array Adapter");
    	}
	}
	
	public void showErrorMsg(int msgResId) {
		Dialogs.getInstance().showErrorMsg(msgResId);
	}

	public void showErrorMsg(String msg) {
		Dialogs.getInstance().showErrorMsg(msg);
	}

	protected void showHourglass() {
    	Dialogs.getInstance().showHourglass();
    }
    protected void dismissHourglass() {
    	Dialogs.getInstance().dismissHourglass();
    }

	public boolean isIntegrateWithHistoryManager() {
		return integrateWithHistoryManager;
	}

	public void setIntegrateWithHistoryManager(boolean integrateWithHistoryManager) {
		this.integrateWithHistoryManager = integrateWithHistoryManager;
	}

	protected void returnToPreviousScreen() {
    	// just pass control back to the previous screen
    	Intent resultIntent = new Intent(this, this.getClass());
    	setResult(Activity.RESULT_OK, resultIntent);
    	finish();    
    }

	@Override
	protected void onResume() {
		super.onResume();
        Log.i(getLocalClassName(), "onResume");
        // Register current activity in onCreate and onresume
        CurrentActivityHolder.getInstance().setCurrentActivity(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
        Log.i(getLocalClassName(), "onPause");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
        Log.i(getLocalClassName(), "onRestart");
	}

	@Override
	protected void onStart() {
		super.onStart();
        Log.i(getLocalClassName(), "onStart");
	}


	@Override
	protected void onStop() {
		super.onStop();
        Log.i(getLocalClassName(), "onStop");
        CurrentActivityHolder.getInstance().iAmNoLongerCurrent(this);
	}
}
