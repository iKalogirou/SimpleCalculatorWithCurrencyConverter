package com.example.calculatorproject
import android.net.ConnectivityManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.NetworkCapabilities
import android.nfc.Tag
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.text.isDigitsOnly
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.min

class MainActivity : AppCompatActivity()
{
     var addOperation = false
     var addDecimal = true
     var infinityFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    // Button Action Functions

    fun changeAction(view: View)
    {
        if (view is Button){
            if (view.text== "💱"){
                if (checkForInternet(this))
                {
                    startActivity(Intent(
                        this,
                        ExchangeActivity::class.java))
                } else
                {
                    Toast.makeText(this,
                        "You must have Internet Access, to use the Live Currency Converter",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    fun numberAction(view: View)
    {
        if (view is Button) {
            if (view.text == ".")
            {
                if (addDecimal)
                {
                    if ((workTV.text.isEmpty())
                        || isTextEndingWithOperator())
                    {
                        workTV.append("0" + view.text)
                        addDecimal = false
                    }
                    else{
                        workTV.append(view.text)
                        addDecimal = false
                    }
                }
                } else
                    workTV.append(view.text)
                    addOperation = true
        }
    }

    fun operatorAction(view: View)
    {
        if (view is Button && view.text =="-")
        {
            if ((workTV.text =="" )
                || (workTV.text.isEmpty()))
            {
                workTV.text="0-"
                addOperation = false
                addDecimal = true
            }
        }
        if(view is Button && addOperation )
        {
            workTV.append(view.text)
            addOperation = false
            addDecimal = true
        }
    }

    fun allClearAction(view: View)
    {
        workTV.text=""
        resultTV.text=""
        addOperation = false
        addDecimal = true
    }

    @SuppressLint("SuspiciousIndentation")
    fun backSpaceAction(view: View)
    {
        addDecimal = true
        val length =workTV.length()
        if (length>0)
            workTV.text= workTV.text.subSequence(0, length -1)

        if (workTV.text.contains("."))
            canUseDecimal()
            canUseOperator()
    }

    @SuppressLint("SetTextI18n")
    fun equalsAction(view: View)
    {
        if (isTextEndingWithOperator())
            Toast.makeText(
                this,
                "Invalid Format for Calculation",
                Toast.LENGTH_SHORT).show()
        else
        {
            resultTV.text = calculateResults()
            workTV.text = resultTV.text
            addDecimal = false
        }

        if(infinityFlag)
        {
            resultTV.text = ""
            workTV.text= ""
            Toast.makeText(
                this,
                "You can't divide a number with 0",
                Toast.LENGTH_SHORT).show()

            infinityFlag = false
            addOperation = false
            addDecimal = true
        }
        if ( resultTV.text.contains("-"))
            workTV.text = ("0" + resultTV.text)
    }

    // Calculation Functions

    private fun calculateResults(): String
    {
        val separate = separateDigitsOperators()
        if (separate.isEmpty()) return ""

        val timesDivision = calculateTimesDivision(separate)
        if (timesDivision.isEmpty()) return ""

        val result = calculateAddSubtract(timesDivision)
        return result.toString()
    }

    private fun separateDigitsOperators(): MutableList<Any>
    {
        val list = mutableListOf<Any>()
        var currentDigit = ""
        for(character in workTV.text)
        {
            if (character.isDigit() || character == '.')
                currentDigit += character
            else
            {
                list.add(currentDigit.toFloat())
                currentDigit = ""
                list.add(character)
            }
        }
        if (currentDigit != "")
            list.add(currentDigit.toFloat())
        return list
    }

    private fun calculateTimesDivision(passedList: MutableList<Any>): MutableList<Any>
    {
        var list = passedList
        while (list.contains('×') || list.contains('÷')) {
            list = calcTimesDiv(list)
        }
        return list
    }

    @SuppressLint("SuspiciousIndentation", "SetTextI18n")
    private fun calcTimesDiv(passedList: MutableList<Any>): MutableList<Any>
    {
        val newList = mutableListOf<Any>()
        var restartIndex = passedList.size

        for (i in passedList.indices)
        {
            if (passedList[i] is Char
                && i != passedList.lastIndex
                && i < restartIndex)
            {
                val operator = passedList[i]
                val previousDigit = passedList[i-1] as Float
                val nextDigit = passedList[i+1] as Float
                when(operator){
                    '×' -> {
                        newList.add(previousDigit * nextDigit)
                        restartIndex = i + 1
                    }
                    '÷' -> {
                        if ((previousDigit/nextDigit == Float.NEGATIVE_INFINITY)
                            || (previousDigit/nextDigit == Float.POSITIVE_INFINITY))
                        {
                            newList.isEmpty()
                            infinityFlag = true
                        }
                        else {
                            newList.add(previousDigit / nextDigit)
                            restartIndex = i + 1
                        }
                    }
                    else ->
                    {
                        newList.add(previousDigit)
                        newList.add(operator)
                    }
                }
            }
            if (i > restartIndex)
                newList.add(passedList[i])
        }
        return newList
    }

    private fun calculateAddSubtract(passedList: MutableList<Any>): Float
    {
        var result = passedList[0] as Float
        for (i in passedList.indices)
        {
            if (passedList[i] is Char && i != passedList.lastIndex){
                val operator = passedList[i]
                val nextDigit = passedList[i+1] as Float
                if ( operator == '+')
                    result += nextDigit
                if ( operator == '-')
                    result -= nextDigit
            }
        }
        return result
    }

    // Logical Functions for Proper Use and Presentation

    private fun canUseOperator()
    {
        addOperation = true
        if (workTV.text.isEmpty() || isTextEndingWithOperator())
        {
            addOperation = false
            addDecimal = true
        }
    }

    private fun canUseDecimal()
    {
        if (workTV.text.endsWith(".") || backSpaceDecimalCheck())
            addDecimal = false
    }

    private fun backSpaceDecimalCheck() : Boolean
    {
        var check = true
        var decimalFound = false
        val list = mutableListOf<Any>()

        for (character in workTV.text)
        {
            if (decimalFound) list.add(character)
            if(character == '.')
            {
                decimalFound = true .also{
                    if (list.isNotEmpty())
                        list.clear()
                }
            }
        }
        val afterDecimalString = list.joinToString()

        if (afterDecimalString.contains("+")
            || afterDecimalString.contains("-")
            || afterDecimalString.contains("×")
            || afterDecimalString.contains("÷"))
            check = false
        return check
    }

    private fun isTextEndingWithOperator () : Boolean
    {
        return (workTV.text.endsWith("+")
                || workTV.text.endsWith("-")
                || workTV.text.endsWith("×")
                || workTV.text.endsWith("÷"))
    }

    private fun checkForInternet(context: Context): Boolean
    {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
}