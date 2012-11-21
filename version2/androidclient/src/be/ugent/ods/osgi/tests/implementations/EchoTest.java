package be.ugent.ods.osgi.tests.implementations;

import android.util.Log;
import android.widget.TextView;
import be.ugent.ods.osgi.protocolabstraction.ModuleAccessor;
import be.ugent.ods.osgi.tests.interfaces.AbstractTest;
import be.ugent.ods.testapplications.service.interfaces.EchoService;

public class EchoTest extends AbstractTest {

	private String response;
	private EchoService echoservice;
	private String text = "10 chars--";
	
	public EchoTest(){
		//Log.d("ET", "create");
	}

	// private String text =
	// "To be, or not to be is the opening phrase of a soliloquy in William Shakespeare's play Hamlet. It is";
	// private String text =
	// "Bravi, bravi, bravissimi Where in the world have you been hiding? Really, you were perfect I only wish I knew your secret Who is this new tutor? Father once spoke of an Angel I used to dream he'd appear Now as I sing I can sense him And I know he's here Here in this room, he calls me softly Somewhere inside, hiding Somehow I know he's always with me He, the unseen genius Christine, you must have been dreaming Stories like this can't come true Christine, you're talking in riddles And it's not like you Angel of music, guide and guardian Grant to me your glory Angel of music, hide no longer Secret and strange AngelHe's with me even now Your hands are coldAll around me Your face, Christine, it's white It frightens me, don't be frightened Read more: PHANTOM OF THE OPERA - ANGEL OF MUSIC LYRICS - dan kies ik ene liedjes tekst dan is die nog een 1000 tekens, vandaar zever, zever gezever lang leve gezegver. Nog een paar tekens te gaan, ja bijna, nog 50, 40 , oooohhhh bijna en we zijn er bijna!";
	@Override
	public void test() {
		//Log.d("TT", text);
		response = echoservice.echoString(text);

	}

	@Override
	public void preRun(ModuleAccessor accessor) {
		echoservice = accessor.getModule(EchoService.class);
	}

	@Override
	public void postRun() {
		TextView text = new TextView(feedback.getActivity());
		text.setText("answer was: " + response);
		feedback.pushTestView(text);
	}

	@Override
	public String getName() {
		return "echotest";
	}

	@Override
	public void changeSize(int size) {
		//Log.d("T", "change text size = " + size);
		if (size == 0) {
			this.text = "10 chars--";
		} else if (size == 1) {
			this.text = "To be, or not to be is the opening phrase of a soliloquy in William Shakespeare's play Hamlet. It is";
		} else {
			this.text = "Bravi, bravi, bravissimi Where in the world have you been hiding? Really, you were perfect I only wish I knew your secret Who is this new tutor? Father once spoke of an Angel I used to dream he'd appear Now as I sing I can sense him And I know he's here Here in this room, he calls me softly Somewhere inside, hiding Somehow I know he's always with me He, the unseen genius Christine, you must have been dreaming Stories like this can't come true Christine, you're talking in riddles And it's not like you Angel of music, guide and guardian Grant to me your glory Angel of music, hide no longer Secret and strange AngelHe's with me even now Your hands are coldAll around me Your face, Christine, it's white It frightens me, don't be frightened Read more: PHANTOM OF THE OPERA - ANGEL OF MUSIC LYRICS - dan kies ik ene liedjes tekst dan is die nog een 1000 tekens, vandaar zever, zever gezever lang leve gezegver. Nog een paar tekens te gaan, ja bijna, nog 50, 40 , oooohhhh bijna en we zijn er bijna!";
		}
		//Log.d("T", text);

	}

}
