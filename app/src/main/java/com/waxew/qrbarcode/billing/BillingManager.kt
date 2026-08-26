/*
 * لایه پرداخت اشتراک هفتگی Google Play Billing.
 * UI مستقیماً با BillingClient کار نمی‌کند و فقط StateFlow این کلاس را مشاهده می‌کند.
 * برای انتشار در فروشگاه دیگر، همین کلاس محل تعویض provider پرداخت است.
 */
package com.waxew.qrbarcode.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


// وضعیت قابل مشاهده توسط Compose.
data class PremiumState(
    val active: Boolean = false,
    val available: Boolean = false,
    val priceText: String = "اشتراک هفتگی",
    val message: String? = null
)

// مالک چرخه اتصال BillingClient و اعتبارسنجی اولیه خریدها در سمت کلاینت.
class BillingManager(context: Context) : PurchasesUpdatedListener {
    companion object {
        const val WEEKLY_PRODUCT_ID = "qr_pro_weekly"
    }

    private val _state = MutableStateFlow(PremiumState())
    val state: StateFlow<PremiumState> = _state.asStateFlow()

    private var productDetails: ProductDetails? = null

    private val billingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    init {
        connect()
    }

    // اتصال به سرویس صورتحساب و سپس خواندن محصول/خریدهای فعال.
    fun connect() {
        if (billingClient.isReady) {
            refresh()
            return
        }
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProduct()
                    refresh()
                } else {
                    _state.value = _state.value.copy(message = "سرویس پرداخت در دسترس نیست.")
                }
            }

            override fun onBillingServiceDisconnected() = Unit
        })
    }

    // جزئیات محصول اشتراک هفتگی و قیمت قابل نمایش را می‌گیرد.
    private fun queryProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(WEEKLY_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        billingClient.queryProductDetailsAsync(params) { result, detailsResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            productDetails = detailsResult.productDetailsList.firstOrNull()
            val offer = productDetails?.subscriptionOfferDetails?.firstOrNull()
            val phase = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()
            _state.value = _state.value.copy(
                available = productDetails != null,
                priceText = phase?.formattedPrice?.let { "$it / هفته" } ?: "اشتراک هفتگی"
            )
        }
    }

    // خریدهای فعال را دوباره استعلام می‌کند؛ در onResume نیز فراخوانی می‌شود.
    fun refresh() {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                handlePurchases(purchases)
            }
        }
    }

    // پنجره استاندارد خرید فروشگاه را باز می‌کند.
    fun launch(activity: Activity) {
        val details = productDetails
        val offerToken = details?.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (details == null || offerToken.isNullOrBlank()) {
            _state.value = _state.value.copy(message = "اشتراک هنوز در فروشگاه برای این نسخه تنظیم نشده است.")
            return
        }

        val item = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()
        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(item))
            .build()
        billingClient.launchBillingFlow(activity, params)
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> handlePurchases(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> _state.value = _state.value.copy(message = "خرید لغو شد.")
            else -> _state.value = _state.value.copy(message = "پرداخت کامل نشد. دوباره تلاش کنید.")
        }
    }

    // خرید تکمیل‌شده را پیدا می‌کند و در صورت نیاز acknowledge انجام می‌دهد.
    private fun handlePurchases(purchases: List<Purchase>) {
        val activePurchase = purchases.firstOrNull { purchase ->
            purchase.products.contains(WEEKLY_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        if (activePurchase != null && !activePurchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(activePurchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    _state.value = _state.value.copy(active = true, message = "اشتراک حرفه‌ای فعال شد.")
                }
            }
        } else {
            _state.value = _state.value.copy(active = activePurchase != null)
        }
    }

    fun close() {
        billingClient.endConnection()
    }
}
