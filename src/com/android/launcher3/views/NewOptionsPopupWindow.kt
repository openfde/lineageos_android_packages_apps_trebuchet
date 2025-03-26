package com.android.launcher3.views;

import android.view.View
import android.widget.PopupWindow
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import android.widget.TextView;
import android.widget.RelativeLayout;
import com.android.launcher3.util.FileUtils;
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.Gravity
import com.android.launcher3.Utilities;
import android.widget.Toast;
import android.content.Intent;
import android.provider.Settings;
import android.view.MotionEvent


class NewOptionsPopupWindow(contentView: View, width: Int, height: Int,val launcher : Launcher) : PopupWindow(contentView, width, height,true) {
    var popupChildWindow: OptionsChildPopupWindow? = null
    
    init {
        initView();
    }

    fun initView(){

        contentView.findViewById<TextView>(R.id.text_display_properties)?.setOnClickListener({
            launcher.startActivity(
                Intent(Intent.ACTION_APPLICATION_PREFERENCES)
                    .setPackage(launcher.getPackageName())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            dismiss()
        })

        contentView.findViewById<TextView>(R.id.text_display_properties)?.setOnHoverListener { v: View, event: MotionEvent ->
            hidePopupWindow()
            false
        }

        contentView.findViewById<TextView>(R.id.text_change_wallpaper)?.setOnClickListener({
            launcher.startWallpaperPicker(it);
            dismiss()
        })

        contentView.findViewById<TextView>(R.id.text_change_wallpaper)?.setOnHoverListener { v: View, event: MotionEvent ->
            hidePopupWindow()
            false
        }

        contentView.findViewById<TextView>(R.id.text_display_settings)?.setOnClickListener({
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
            launcher.startActivity(intent)
            dismiss()
        })

        contentView.findViewById<TextView>(R.id.text_display_settings)?.setOnHoverListener { v: View, event: MotionEvent ->
            hidePopupWindow()
            false
        }

        contentView.findViewById<RelativeLayout>(R.id.layout_system_theme)?.setOnClickListener({
            showPopupWindow(R.array.theme_options,it)
        })

        contentView.findViewById<RelativeLayout>(R.id.layout_system_theme)?.setOnHoverListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    // 鼠标悬停进入
                    showPopupWindow(R.array.theme_options,v)
                    true
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    // 鼠标悬停离开
                   
                    true
                }
                else -> false
            }
        }

        contentView.findViewById<RelativeLayout>(R.id.layout_new)?.setOnClickListener({
            showPopupWindow(R.array.file_options,it)
        })

        contentView.findViewById<RelativeLayout>(R.id.layout_new)?.setOnHoverListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    // 鼠标悬停进入
                    showPopupWindow(R.array.file_options,v)
                    true
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    // 鼠标悬停离开
                    // if (popupChildWindow != null && popupChildWindow!!.isShowing) {
                    //     popupChildWindow!!.dismiss()
                    // }
                    true
                }
                else -> false
            }
        }

        contentView.findViewById<RelativeLayout>(R.id.layout_sort)?.setOnClickListener({
            showPopupWindow(R.array.sort_options,it)
        })
        contentView.findViewById<RelativeLayout>(R.id.layout_sort)?.setOnHoverListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    // 鼠标悬停进入
                    showPopupWindow(R.array.sort_options,v)
                    true
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    // 鼠标悬停离开
                    // if (popupChildWindow != null && popupChildWindow!!.isShowing) {
                    //     popupChildWindow!!.dismiss()
                    // }
                    true
                }
                else -> false
            }
        }


       
        contentView.findViewById<TextView>(R.id.text_open_the_terminal)?.setOnClickListener({

            dismiss()
        })

        contentView.findViewById<TextView>(R.id.text_open_the_terminal)?.setOnHoverListener { v: View, event: MotionEvent ->
            hidePopupWindow()
            false
        }

    }

    fun showPopupWindow(redId :Int,v :View){
        if (popupChildWindow != null && popupChildWindow!!.isShowing) {
            popupChildWindow!!.dismiss()
        }
        popupChildWindow = OptionsChildPopupWindow(
            LayoutInflater.from(launcher).inflate(R.layout.popup_child_layout, null),
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            redId,
            this,
            launcher
        )

        if (v != null && !launcher.isDestroyed && !launcher.isFinishing) {
            popupChildWindow?.showAsDropDown(v, 164, -30, Gravity.NO_GRAVITY)
        }
    }

    fun hidePopupWindow(){
        if (popupChildWindow != null && popupChildWindow!!.isShowing) {
            popupChildWindow!!.dismiss()
        }
    }

    override fun showAtLocation(parent: View?, gravity: Int, x: Int, y: Int) {
        super.showAtLocation(parent, gravity, x, y)
    
    }

    override fun dismiss() {
        super.dismiss()
        hidePopupWindow()
    }
}
