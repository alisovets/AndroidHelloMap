package alisovets.example.hellogooglemap;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class SelectPlaceActivity  extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new SelectPlaceFragment()).commit();
		}
	}
}
