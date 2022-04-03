package com.climbing.yaho.ui

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.climbing.yaho.R
import com.climbing.yaho.databinding.LayoutAdRemovePopupBinding
import com.climbing.yaho.local.YahoPreference
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject


@AndroidEntryPoint
class AdRemovePopup(private val onClickRemoveAds : () -> Unit) : BottomSheetDialogFragment() {

    @Inject
    lateinit var yahoPreference: YahoPreference
    private var binding: LayoutAdRemovePopupBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = LayoutAdRemovePopupBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.NewDialog)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        if(yahoPreference.isSubscribing) return
        if(TimeUnit.MILLISECONDS.toDays(yahoPreference.noMoreAdsPopupToday) == TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis())) dialog?.dismiss()

        // 팝업 생성 시 전체화면으로 띄우기
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val behavior = BottomSheetBehavior.from<View>(bottomSheet!!)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // 드래그해도 팝업이 종료되지 않도록
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        })

        binding?.apply {
            btnClose.setOnClickListener {
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
            }
            btnNoMoreToday.setOnClickListener {
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
                yahoPreference.noMoreAdsPopupToday = System.currentTimeMillis()
            }
            btnRemoveAds.setOnClickListener {
                behavior.state = BottomSheetBehavior.STATE_HIDDEN
                onClickRemoveAds.invoke()
            }
        }

    }
}