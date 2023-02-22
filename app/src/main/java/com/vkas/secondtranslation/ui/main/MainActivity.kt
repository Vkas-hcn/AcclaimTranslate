package com.vkas.secondtranslation.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vkas.secondtranslation.BR
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.base.BaseActivity
import com.vkas.secondtranslation.databinding.ActivityMainBinding
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.ui.camare.CameraActivity
import com.vkas.secondtranslation.ui.translation.TranslationActivity
import com.vkas.secondtranslation.web.WebStActivity
import com.xuexiang.xutil.tip.ToastUtils

class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_main
    }

    override fun initVariableId(): Int {
        return BR._all
    }

    override fun initParam() {
        super.initParam()
    }

    override fun initToolbar() {
        super.initToolbar()
        binding.presenter = Presenter()
        binding.inMainTitle.imgBack.visibility = View.GONE
    }

    override fun initData() {
        super.initData()
    }

    inner class Presenter {
        fun clickMainMenu() {}
        fun clickMain() {
            if (binding.sidebarShowsSt == true) {
                binding.sidebarShowsSt = false
            }
        }

        fun clickTranslation() {
            startActivity(TranslationActivity::class.java)
        }

        fun clickOcr() {
            val hasWriteStoragePermission: Int =
                ContextCompat.checkSelfPermission(application, Manifest.permission.CAMERA)
            if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
                startActivity(CameraActivity::class.java)
            } else {
                //没有权限，向用户请求权限
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    1
                )
            }
        }

        fun clickVpn() {

        }

        fun clickHome() {
            binding.sidebarShowsSt = false
            binding.inMainTitle.tvTitle.text = getString(R.string.translate)
        }

        fun clickSetting() {
            binding.sidebarShowsSt = true
            binding.inMainTitle.tvTitle.text = getString(R.string.settings)
        }

        fun toContactUs() {
            val uri = Uri.parse("mailto:${Constant.MAILBOX_ST_ADDRESS}")
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            runCatching {
                startActivity(intent)
            }.onFailure {
                ToastUtils.toast("Please send your problem to our email:${Constant.MAILBOX_ST_ADDRESS}")
            }
        }

        fun toPrivacyPolicy() {
            startActivity(WebStActivity::class.java)
        }

        fun toShare() {
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(
                Intent.EXTRA_TEXT,
                Constant.SHARE_ST_ADDRESS + this@MainActivity.packageName
            )
            intent.type = "text/plain"
            startActivity(intent)
        }
    }
    private fun goSystemSetting() {
        val intent = Intent().apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            data = Uri.fromParts("package", this@MainActivity.packageName, null)
        }
        startActivity(intent)
    }
    /**
     * 拒绝权限弹框
     */
    private fun denyPermissionPopUp() {
        val dialog: AlertDialog? = AlertDialog.Builder(this)
            .setTitle(getString(R.string.permission_title))
            .setMessage(getString(R.string.permission_message))
            //设置对话框的按钮
            .setNegativeButton("CANCEL") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("SET UP") { dialog, _ ->
                dialog.dismiss()
                goSystemSetting()
            }.create()
        dialog?.show()
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(CameraActivity::class.java)
            } else {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    denyPermissionPopUp()
                }
            }
        }
    }
}