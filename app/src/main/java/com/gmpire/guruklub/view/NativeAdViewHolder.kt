package com.gmpire.guruklub.view

import com.gmpire.guruklub.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import com.gmpire.guruklub.databinding.ItemEmptyNativeAdsBinding
import com.gmpire.guruklub.view.adapter.IAdapterListener
import com.gmpire.guruklub.view.base.BaseViewHolder
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView

/**
 * Created by Tahsin Rahman on 27/9/20.
 */


class NativeAdViewHolder(itemView: ViewDataBinding, context: Context?) :
    BaseViewHolder(itemView.root) {

    val context: Context? = context
    var binding = itemView as ItemEmptyNativeAdsBinding

    override fun <T> onBind(position: Int, model: T, mCallback: IAdapterListener) {
        val builder = AdLoader.Builder(context, context?.getString(R.string.ad_unit_id_native_test))
            .forUnifiedNativeAd { unifiedNativeAd ->
                // Assumes that your ad layout is in a file call ad_unified.xml
                // in the res/layout folder
                binding.relativeLayoutNativePlaceHolder.visibility = View.GONE
                val inflater = itemView.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)
                        as LayoutInflater
                val adView = inflater
                    .inflate(
                        R.layout.layout_native_ad,
                        null
                    ) as UnifiedNativeAdView
                // This method sets the text, images and the native ad, etc into the ad
                // view.
                populateUnifiedNativeAdView(unifiedNativeAd, adView)
                // Assumes you have a placeholder FrameLayout in your View layout
                // (with id ad_frame) where the ad is to be placed.
                binding.frameLayoutBlankNative.visibility =View.GONE
                binding.frameNativeAdContainer.removeAllViews()
                binding.frameNativeAdContainer.addView(adView)
            }
            .build()
        builder.loadAd(AdRequest.Builder().build())
    }

    override fun onBind(position: Int, mCallback: IAdapterListener) {

    }

    private fun populateUnifiedNativeAdView(
        adFromGoogle: UnifiedNativeAd,
        myAdView: UnifiedNativeAdView
    ) {
        val mediaView: MediaView = myAdView.findViewById(R.id.ad_media)
        myAdView.mediaView = mediaView
        myAdView.headlineView = myAdView.findViewById(R.id.ad_headline)
        myAdView.bodyView = myAdView.findViewById(R.id.ad_body)
        myAdView.callToActionView = myAdView.findViewById(R.id.ad_call_to_action)
        myAdView.iconView = myAdView.findViewById(R.id.ad_icon)
        myAdView.priceView = myAdView.findViewById(R.id.ad_price)
        myAdView.starRatingView = myAdView.findViewById(R.id.ad_rating)
        myAdView.storeView = myAdView.findViewById(R.id.ad_store)
        myAdView.advertiserView = myAdView.findViewById(R.id.ad_advertiser)
        (myAdView.headlineView as TextView).text = adFromGoogle.headline
        if (adFromGoogle.body == null) {
            myAdView.bodyView.visibility = View.GONE
        } else {
            (myAdView.bodyView as TextView).text = adFromGoogle.body
        }
        if (adFromGoogle.callToAction == null) {
            myAdView.callToActionView.visibility = View.GONE
        } else {
            (myAdView.callToActionView as Button).setText(adFromGoogle.callToAction)
        }
        if (adFromGoogle.icon == null) {
            myAdView.iconView.visibility = View.GONE
        } else {
            (myAdView.iconView as ImageView).setImageDrawable(
                adFromGoogle.icon.drawable
            )
        }
        if (adFromGoogle.price == null) {
            myAdView.priceView.visibility = View.GONE
        } else {
            (myAdView.priceView as TextView).text = adFromGoogle.price
        }
        if (adFromGoogle.starRating == null) {
            myAdView.starRatingView.visibility = View.GONE
        } else {
            (myAdView.starRatingView as RatingBar).rating = adFromGoogle.starRating.toFloat()
        }
        if (adFromGoogle.store == null) {
            myAdView.storeView.visibility = View.GONE
        } else {
            (myAdView.storeView as TextView).text = adFromGoogle.store
        }
        if (adFromGoogle.advertiser == null) {
            myAdView.advertiserView.visibility = View.GONE
        } else {
            (myAdView.advertiserView as TextView).text = adFromGoogle.advertiser
        }
        myAdView.setNativeAd(adFromGoogle)
    }

}