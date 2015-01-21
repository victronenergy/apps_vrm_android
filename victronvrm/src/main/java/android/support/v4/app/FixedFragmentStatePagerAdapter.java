package android.support.v4.app;

import android.os.Bundle;
import android.view.ViewGroup;

/**
 * By using FragmentStatePagerAdapter with custom View, BadParcelableException may be thrown.<br/>
 * This class is used to fix that bug<br/>
 * <br/>
 * <a>https://code.google.com/p/android/issues/detail?id=37484<a/>
 */
public abstract class FixedFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

	public FixedFragmentStatePagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Fragment f = (Fragment) super.instantiateItem(container, position);
		Bundle savedFragmentState = f.mSavedFragmentState;
		if (savedFragmentState != null) {
			savedFragmentState.setClassLoader(f.getClass().getClassLoader());
		}
		return f;
	}
}
