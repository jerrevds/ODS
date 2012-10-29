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

public class VideoTest implements TestInterface {
	
	private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
	
	private ModuleAccessor accessor;
	private FeedbackInterface feedback;
	
	@Override
	public void runTest(ModuleAccessor accessor, FeedbackInterface feedback) {
		this.accessor = accessor;
		this.feedback = feedback;
		
		//create new Intent
		Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
		
		intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1); // set the video image quality to high
		// start the Video Capture Intent
		feedback.getActivity().startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
	}

	@Override
	public void runActivityForResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
    	    if (resultCode == Activity.RESULT_OK) {
    	    	Uri videoUri = data.getData();
    	    	
    	    	//TODO data naar server sturen en terug
    	    	VideoService service = (VideoService)accessor.getModule(VideoService.class);
    	    	//videoUri = service.doSomething(videoUri); interface werkt blijkbaar met ander URI object, niet echt belangrijk op het moment
    	    	
    	    	VideoView myVideoView = new VideoView(feedback.getActivity());
    	    	myVideoView.setVisibility(0);
    	    	myVideoView.setVideoURI(videoUri);
    	    	myVideoView.setMediaController(new MediaController(feedback.getActivity()));
    	    	myVideoView.requestFocus();
    	    	myVideoView.start();
    	    	
    	    	feedback.pushTestView(myVideoView);
    	    } else if (resultCode == Activity.RESULT_CANCELED) {
    	        Log.d("HELP","hier2");
    	    } else {
    	        Log.d("HELP","hier3");
    	    }
    	}
		
	}

}
