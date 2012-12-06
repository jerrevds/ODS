package be.ugent.ods.osgi.tests.implementations;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import be.ugent.ods.osgi.R;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.implementations.imagehelp.AndroidUtils;
import be.ugent.ods.osgi.tests.interfaces.AbstractTest;
import be.ugent.ods.testapplications.service.interfaces.GlowFilterService;

public class GlowFilterTest extends AbstractTest {
	private int[] mColors;
	private GlowFilterService glow;
	private ImageView mOriginalImageView;
	private int width;
	private int height;
	private int size;

	public GlowFilterTest() {
		this.size = 0;
	}

	private float getAmout(int value) {
		float retValue = 0;
		retValue = (float) (value / 100f);
		return retValue;
	}

	@Override
	public void test() {
	
		glow.setAmount(getAmout(70));
		glow.setRadius(70);
		mColors = glow.filter(mColors, width, height);

	}

	@Override
	public void preRun(ModuleAccessor accessor) {
		mOriginalImageView = new ImageView(feedback.getActivity());
		if (size == 0) {
			mOriginalImageView.setImageResource(R.drawable.i23kb);
		} else if (size == 1) {
			mOriginalImageView.setImageResource(R.drawable.i200kb);
		} else {
			mOriginalImageView.setImageResource(R.drawable.i1mb);
		}
		mColors = AndroidUtils.drawableToIntArray(mOriginalImageView
				.getDrawable());
		glow = accessor.getModule(GlowFilterService.class);
		width = mOriginalImageView.getDrawable().getIntrinsicWidth();
		height = mOriginalImageView.getDrawable().getIntrinsicHeight();
	}

	@Override
	public void postRun() {
		if (mColors != null) {
			Bitmap mFilterBitmap = Bitmap.createBitmap(mColors, 0, width,
					width, height, Bitmap.Config.ARGB_8888);
			ImageView mModifyImageView = new ImageView(feedback.getActivity());
			ScrollView scrol = new ScrollView(feedback.getActivity());
			mModifyImageView.setImageBitmap(mFilterBitmap);
			LinearLayout layout = new LinearLayout(feedback.getActivity());
			layout.setOrientation(LinearLayout.VERTICAL);
			layout.addView(mModifyImageView);
			layout.addView(mOriginalImageView);
			scrol.addView(layout);
			feedback.pushTestView(scrol);
		} else {
			feedback.pushTestView(new TextView(feedback.getActivity()));
		}

	}

	@Override
	public String getName() {
		return "image";
	}

	@Override
	public void changeSize(int size) {
		this.size = size;

	}

}
