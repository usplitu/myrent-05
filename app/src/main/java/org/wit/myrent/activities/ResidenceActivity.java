package org.wit.myrent.activities;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;

import org.wit.myrent.R;
import org.wit.myrent.app.MyRentApp;
import org.wit.myrent.models.Portfolio;
import org.wit.myrent.models.Residence;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;
import static org.wit.android.helpers.IntentHelper.navigateUp;
import static org.wit.android.helpers.IntentHelper.selectContact;
import static org.wit.android.helpers.ContactHelper.getContact;
import static org.wit.android.helpers.IntentHelper.sendEmail;


public class ResidenceActivity extends Activity implements TextWatcher, OnCheckedChangeListener, View.OnClickListener, DatePickerDialog.OnDateSetListener
{
  private EditText geolocation;
  private CheckBox rented;
  private Button   dateButton;

  private Residence residence;

  private Portfolio portfolio;

  private static final int REQUEST_CONTACT = 1;
  private Button   tenantButton;
  private Button   reportButton;


  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_residence);
    getActionBar().setDisplayHomeAsUpEnabled(true);

    geolocation = (EditText) findViewById(R.id.geolocation);
    dateButton  = (Button)   findViewById(R.id.registration_date);
    rented      = (CheckBox) findViewById(R.id.isrented);

    residence = new Residence();

    geolocation.addTextChangedListener(this);
    geolocation.setText(residence.geolocation);

    rented     .setChecked(residence.rented);
    rented     .setOnCheckedChangeListener(this);

    tenantButton = (Button)   findViewById(R.id.tenant);
    reportButton = (Button)   findViewById(R.id.residence_reportButton);

    MyRentApp app = (MyRentApp) getApplication();
    portfolio = app.portfolio;
    UUID resId = (UUID) getIntent().getExtras().getSerializable("RESIDENCE_ID");
    residence = portfolio.getResidence(resId);
    if (residence != null)
    {
      updateControls(residence);
    }

    dateButton  .setOnClickListener(this);

  }

  public void updateControls(Residence residence)
  {
    geolocation.setText(residence.geolocation);
    rented.setChecked(residence.rented);
    dateButton.setText(residence.getDateString());
    tenantButton.setOnClickListener(this);
    reportButton.setOnClickListener(this);
  }

  @Override
  public void onCheckedChanged(CompoundButton arg0, boolean isChecked)
  {
    Log.i(this.getClass().getSimpleName(), "rented Checked");
    residence.rented = isChecked;
  }

  @Override
  public void afterTextChanged(Editable c)
  {
    Log.i(this.getClass().getSimpleName(), "geolocation " + c.toString());
    residence.setGeolocation(c.toString());
  }

  @Override
  public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
  {
  }

  @Override
  public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3)
  {
  }

  @Override
  public void onClick(View v)
  {
    switch (v.getId())
    {
      case R.id.registration_date      : Calendar c = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog (this, this, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        dpd.show();
        break;

      case R.id.tenant                 : selectContact(this, REQUEST_CONTACT);
        break;

      case R.id.residence_reportButton : sendEmail(this, "", getString(R.string.residence_report_subject), residence.getResidenceReport(this));
        break;
    }
  }

  @Override
  public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
  {
    Date date = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
    residence.date = date;
    dateButton.setText(residence.getDateString());
  }

  public void onPause()
  {
    super.onPause();
    portfolio.saveResidences();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    switch (item.getItemId())
    {
      case android.R.id.home:  navigateUp(this);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data)
  {
    switch (requestCode)
    {
      case REQUEST_CONTACT    : String name= getContact(this, data);
        residence.tenant = name;
        tenantButton.setText(name);
        break;
    }
  }

}