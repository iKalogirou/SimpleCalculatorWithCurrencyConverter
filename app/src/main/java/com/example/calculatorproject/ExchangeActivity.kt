package com.example.calculatorproject

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.ArrayRes
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_exchange.*
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL


var baseCurrency = "EUR"
var convertedToCurrency = "USD"
var conversionRate = 0f

class ExchangeActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange)
        initUI()
    }

    private fun initUI()
    {
        initSpinners()
        initTextChanged()
    }

    fun returnCalculator(view: View)
    {
        if (view is Button)
        {
            if (view.text== "Return to CALCULATOR")
            {
                finish()
            }
        }
    }

    private fun initTextChanged()
    {
        et_firstConversion.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                try {
                    getApiResult()
                } catch (e: Exception){
                    Log.e("ExchangeActivity", "$e")
                }
            }
        })
    }

    @SuppressLint("ResourceType")
    private fun initSpinners()
    {
        val spinner1: Spinner = findViewById(R.id.spinner_firstConversion)
        val spinner2: Spinner = findViewById(R.id.spinner_secondConversion)

        spinner1.setUpSpinner(
            currencies = R.array.currencies,
            onSelect = {baseCurrency = it}
        )
        spinner2.setUpSpinner(
            currencies = R.array.currencies2,
            onSelect = { convertedToCurrency = it}
        )
    }

    private fun Spinner.setUpSpinner(@ArrayRes currencies: Int, onSelect: (String) -> Unit)
    {
        ArrayAdapter.createFromResource(
            this@ExchangeActivity,
            currencies,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter
        }

        this.onItemSelectedListener = (object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                onSelect(parent?.getItemAtPosition(position).toString())
                getApiResult()
            }
        })
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getApiResult()
    {
        if (et_firstConversion != null
            && et_firstConversion.text.isNotEmpty()
            && et_firstConversion.text.isNotBlank())
        {
            val  api = "https://api.apilayer.com/fixer/latest?apikey=Uc8RSHenYD9snCpzMgX72TxAMCI5o4Bs&base=$baseCurrency"
            if (baseCurrency == convertedToCurrency)
            {
                Toast.makeText(
                    applicationContext,
                    "Can't Convert the same Currency",
                    Toast.LENGTH_SHORT).show()
            } else
            {
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val apiResult = URL(api).readText()
                        val jsonObject = JSONObject(apiResult)

                        conversionRate = jsonObject.getJSONObject("rates")
                                .getString(convertedToCurrency)
                                .toFloat()

                        withContext(Dispatchers.Main) {
                            val text =  ((et_firstConversion.text.toString()
                                .toFloat()) * conversionRate )
                                .toString()
                            et_secondConversion?.setText(text)
                        }
                    } catch (e: Exception)
                    {
                        Log.e("ExchangeActivity", "$e")
                    }
                }
            }
        }
    }
}