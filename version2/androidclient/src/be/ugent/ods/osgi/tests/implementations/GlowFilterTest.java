package be.ugent.ods.osgi.tests.implementations;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import be.ugent.ods.osgi.R;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.implementations.imagehelp.AndroidUtils;
import be.ugent.ods.osgi.tests.interfaces.FeedbackInterface;
import be.ugent.ods.osgi.tests.interfaces.TestInterface;
import be.ugent.ods.testapplications.service.interfaces.EchoService;
import be.ugent.ods.testapplications.service.interfaces.GlowFilterService;

public class GlowFilterTest implements TestInterface {
	private int[] mColors;

	@Override
	public void runTest(ModuleAccessor accessor, FeedbackInterface feedback) {
		ImageView mOriginalImageView = new ImageView(feedback.getActivity());
		mOriginalImageView.setImageResource(R.drawable.image);
		final int width = mOriginalImageView.getDrawable().getIntrinsicWidth();
		final int height = mOriginalImageView.getDrawable()
				.getIntrinsicHeight();

		mColors = AndroidUtils.drawableToIntArray(mOriginalImageView
				.getDrawable());
		int mRadiusValue = 70;
		int mAmountValue = 70;
		GlowFilterService glow = accessor.getModule(GlowFilterService.class);
		glow.setAmount(getAmout(mAmountValue));
		glow.setRadius(mRadiusValue);
		mColors = glow.filter(mColors, width, height);
		Bitmap mFilterBitmap = Bitmap.createBitmap(mColors, 0, width, width, height,
				Bitmap.Config.ARGB_8888);
		ImageView mModifyImageView = new ImageView(feedback.getActivity());
		ScrollView scrol = new ScrollView(feedback.getActivity());
		mModifyImageView.setImageBitmap(mFilterBitmap);
		LinearLayout layout = new LinearLayout(feedback.getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(mModifyImageView);
		layout.addView(mOriginalImageView);
		scrol.addView(layout);
		feedback.pushTestView(scrol);
	}

	private float getAmout(int value) {
		float retValue = 0;
		retValue = (float) (value / 100f);
		return retValue;
	}

}
