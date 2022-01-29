package simpleapps.tictactoe

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import simpleapps.tictactoe.MainActivity.Companion.billingClient
import simpleapps.tictactoe.MainActivity.Companion.reshadeLines
import simpleapps.tictactoe.MainActivity.Companion.skuDetailsList
import simpleapps.tictactoe.MainActivity.Companion.skuList
import simpleapps.tictactoe.databinding.ChangeBoardLayoutBinding


class ChangeBoardColor : AppCompatActivity() {


    companion object {
        var activitya: Activity? = null
        lateinit var purchases: MutableList<Purchase>
        val purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingResponseCode.OK && purchases != null) {
                    val purchase = purchases[0]
                    val b = Bundle()
                    b.putString("buyFlow", "PURCHASE_SUCCESS")
                    b.putString("buyItem", purchase.skus[0])
                    logEvent("buyFlow", b)
                    if (purchase.skus[0] == boughtItem?.sku) {
                        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            if (!purchase.isAcknowledged) {
                                val acknowledgePurchaseParams =
                                    AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                billingClient?.acknowledgePurchase(
                                    acknowledgePurchaseParams.build()
                                ) { p0 ->
                                    Log.d(
                                        "texts",
                                        "onAcknowledgePurchaseResponse: " + p0.responseCode
                                    )
                                    val b1 = Bundle()
                                    b1.putString("buyFlow", "ACK_SUCCESS")
                                    b1.putString("buyItem", purchase.skus[0])
                                    logEvent("buyFlow", b1)

                                    Snackbar.make(
                                        inflate.root,
                                        "Purchase Success",
                                        Snackbar.LENGTH_LONG
                                    ).show()
                                    val activity = activitya
                                    Log.d("texts", ": $activity")
                                    if (activity != null) {
                                        saveExit(inflate.colorSpinner, activity)
                                    }
                                }
                            }
                        }
                    }
                } else if (billingResult.responseCode == BillingResponseCode.USER_CANCELED) {
                    val b1 = Bundle()
                    b1.putString("buyFlow", "CANCELLED")
                    logEvent("buyFlow", b1)
                    Snackbar.make(inflate.root, "Payment Cancelled", Snackbar.LENGTH_LONG).show()
                } else {
                    val b1 = Bundle()
                    b1.putString("buyFlow", "ERROR")
                    b1.putString("error", billingResult.responseCode.toString())
                    logEvent("buyFlow", b1)
                    Snackbar.make(inflate.root, "Some Error Occured", Snackbar.LENGTH_LONG).show()
                }
            }
        lateinit var inflate: ChangeBoardLayoutBinding
        var selposBought = -1
        var boughtItem: SkuDetails? = null
        private fun saveExit(colorSpinner: Spinner, activity: Activity) {
            val b = Bundle()
            b.putString("selectedValue", colorSpinner.selectedItem.toString())
            logEvent("saved", b)
            val sharedPreferences = activity.getSharedPreferences("options", 0)
            sharedPreferences.edit()
                .putInt("color", colorSpinner.selectedItemPosition)
                .apply()
            activity.finish()
        }

        private fun logEvent(eventName: String, bundle: Bundle) {
            activitya?.let { FirebaseAnalytics.getInstance(it).logEvent(eventName, bundle) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflate = ChangeBoardLayoutBinding.inflate(layoutInflater)
        activitya = this
        setContentView(inflate.root)
        val colorSpinner = inflate.colorSpinner
        val list: MutableList<String> = ArrayList()
        list.add("Classic Stripe(Free)")
        list.add("Pink Marble")
        list.add("Cheetah Stripe")
        list.add("Special Stripe")
        list.add("Neon Stripe")
        list.add("Black Color(Free)")
        list.add("Grey Color(Free)")
        list.add("Green Color(Free)")
        list.add("App Main Color(Free)")
        list.add("App Secondary Color(Free)")
        val dataAdapter =
            ArrayAdapter(this, R.layout.simple_spinner_item_editable, list)
        dataAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item_editable)
        colorSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val b1 = Bundle()
                b1.putString("buyFlow", "SELECTED")
                b1.putString("selected_item", colorSpinner.selectedItem.toString())
                reshadeLines(this@ChangeBoardColor, position)
                val saveBoard = inflate.saveBoard
                if (position in 1..4) {
                    b1.putString("selected_type", "PAID")
                    logEvent("buyFlow", b1)
                    val filter = skuDetailsList?.filter {
                        it.sku == skuList?.get(position - 1)
                    }
                    var bought = 0
                    val skuDetails = filter?.get(0)
                    purchases.forEach {
                        if (it.skus[0] == skuDetails?.sku) {
                            bought++
                            return@forEach
                        }
                    }
                    if (bought == 0) {
                        saveBoard.text =
                            "Buy & Save at ${skuDetails?.price}"
                        saveBoard.setOnClickListener {
                            if (skuDetails != null) {
                                selposBought = position
                                boughtItem = skuDetails
                                val billingFlowParams = BillingFlowParams.newBuilder()
                                    .setSkuDetails(skuDetails)
                                    .build()
                                val b = Bundle()
                                b.putString("buyFlow", "START")
                                b.putString("buyItem", skuDetails.sku)
                                logEvent("buyFlow", b)
                                billingClient?.launchBillingFlow(
                                    this@ChangeBoardColor,
                                    billingFlowParams
                                )
                            }
                        }
                    } else {
                        saveBoard.text = "Save Board"
                        saveBoard.setOnClickListener {
                            saveExit(colorSpinner, this@ChangeBoardColor)
                        }
                    }
                } else {
                    b1.putString("selected_type", "FREE")
                    logEvent("buyFlow", b1)
                    saveBoard.text = "Save Board"
                    saveBoard.setOnClickListener {
                        saveExit(colorSpinner, this@ChangeBoardColor)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }
        colorSpinner.adapter = dataAdapter
        val sharedPreferences = getSharedPreferences("options", 0)
        reshadeLines(this@ChangeBoardColor, sharedPreferences.getInt("color", 0))

        inflate.saveBoard.setOnClickListener {
            val sharedPreferences = getSharedPreferences("options", 0)
            sharedPreferences.edit().putInt("color", colorSpinner.selectedItemPosition).apply()
        }
    }

}
