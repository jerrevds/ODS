package be.ugent.ods.osgi.tests.implementations;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.interfaces.FeedbackInterface;
import be.ugent.ods.osgi.tests.interfaces.TestInterface;
import be.ugent.ods.testapplications.service.interfaces.VideoService;

public class VideoTest extends TestInterface {

	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;



	private VideoService service;

	private Uri videoUri;

	@Override
	public void runActivityForResult(int requestCode, int resultCode,
			Intent data) {
		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				videoUri = data.getData();

				waitingForResult=false;

				
			} else if (resultCode == Activity.RESULT_CANCELED) {
				waitingForResult=false;
				Log.d("HELP", "hier2");
			} else {
				waitingForResult=false;
				Log.d("HELP", "hier3");
			}
		}

	}

	@Override
	public void test() {
		// TODO data naar server sturen en terug
		
		// videoUri = service.doSomething(videoUri); interface werkt
		// blijkbaar met ander URI object, niet echt belangrijk op het
		// moment

	}

	@Override
	public void preRun(ModuleAccessor accessor) {
		// create new Intent
		waitingForResult=true;
		service = (VideoService) accessor
				.getModule(VideoService.class);
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video
															// image quality to
															// high
		// start the Video Capture Intent
		feedback.getActivity().startActivityForResult(intent,
				CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);

	}

	@Override
	public void postRun() {
		
	
		feedback.getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				//videoview needs for some f***** reason a main thread
				VideoView myVideoView= new VideoView(feedback.getActivity());
				myVideoView.setVisibility(0);
				myVideoView.setVideoURI(videoUri);
				myVideoView.setMediaController(new MediaController(feedback
						.getActivity()));
				myVideoView.requestFocus();
				myVideoView.start();
				feedback.pushTestView(myVideoView);
			}
		});
		
		



	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "video";
	}

}
