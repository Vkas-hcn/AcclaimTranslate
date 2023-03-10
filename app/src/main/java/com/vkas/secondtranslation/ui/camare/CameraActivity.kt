package com.vkas.secondtranslation.ui.camare

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.common.util.concurrent.ListenableFuture
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vkas.secondtranslation.BR
import com.vkas.secondtranslation.R
import com.vkas.secondtranslation.app.App
import com.vkas.secondtranslation.base.BaseActivity
import com.vkas.secondtranslation.databinding.ActivityCameraxBinding
import com.vkas.secondtranslation.event.Constant
import com.vkas.secondtranslation.ui.language.LanguageActivity
import com.vkas.secondtranslation.utils.CopyUtils
import com.vkas.secondtranslation.utils.KLog
import com.vkas.secondtranslation.utils.MlKitData
import com.vkas.secondtranslation.widget.StLoadingDialog
import com.xuexiang.xutil.app.ActivityUtils
import com.xuexiang.xutil.tip.ToastUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class CameraActivity : BaseActivity<ActivityCameraxBinding, CameraXViewModel>() {
    private lateinit var ptLoadingDialog: StLoadingDialog
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private var cameraProvider: ProcessCameraProvider? = null
    private var jobBack: Job? = null

    override fun initContentView(savedInstanceState: Bundle?): Int {
        return R.layout.activity_camerax
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
    }

    override fun initData() {
        super.initData()
        liveEventBusReceive()
        ptLoadingDialog = StLoadingDialog(this)
        viewModel.initializeLanguageBox()
        MlKitData.getInstance().fetchDownloadedModels()
        updateLanguageItem()
        initCameraX()
    }

    private fun liveEventBusReceive() {
        //?????????????????????
//        LiveEventBus
//            .get(Constant.PLUG_ST_TRANSLATION_SHOW, Boolean::class.java)
//            .observeForever {
//                StLoadBackAd.getInstance().advertisementLoadingSt(this)
////                if(!it){
//                finish()
////                }
//            }
    }

    /**
     * ????????????
     */
//    private fun returnToHomePage() {
//        App.isAppOpenSameDaySt()
//        if (PixelUtils.isThresholdReached()) {
//            KLog.d(Constant.logTagSt, "??????????????????")
//            finish()
//            return
//        }
//        StLoadBackAd.getInstance().advertisementLoadingSt(this)
//        jobBack = GlobalScope.launch {
//            try {
//                withTimeout(3000L) {
//                    while (isActive) {
//                        val showState =
//                            StLoadBackAd.getInstance()
//                                .displayBackAdvertisementSt(this@CameraXActivity)
//                        if (showState) {
//                            jobBack?.cancel()
//                            jobBack = null
//                        }
//                        delay(1000L)
//                    }
//                }
//            } catch (e: TimeoutCancellationException) {
//                KLog.d(Constant.logTagSt, "ocr-back---????????????")
//                if (jobBack != null) {
//                    finish()
//                }
//            }
//        }
//    }

    override fun initViewObservable() {
        super.initViewObservable()
        showTranslationResult()
    }

    private fun showTranslationResult() {
        MlKitData.getInstance().sourceText.observe(this, {
            binding.edCameraDown.setText(it)
            ptLoadingDialog.dismiss()
        })
    }

    inner class Presenter {
        fun toReturn() {
            if (binding.conCamera.isVisible) {
//                returnToHomePage()
                finish()
            } else {
                binding.conShot.visibility = View.GONE
                binding.conCamera.visibility = View.VISIBLE
                initCameraX()
            }
        }

        fun toLanguage(type: Int) {
            ActivityUtils.startActivityForResult(
                this@CameraActivity,
                LanguageActivity().javaClass,
                Constant.JUMP_LANGUAGE_PAGE,
                Constant.JUMP_LANGUAGE_PARAMETERS,
                type
            )
        }

        fun toExchange() {
            if (binding.selectedSourceLang == 1) {
                binding.selectedSourceLang = 2
            } else {
                binding.selectedSourceLang = 1
            }
            viewModel.exchangeLanguage()
            updateLanguageItem()
        }

        fun toImageFile() {
            pickPhoto()
        }

        fun toPhotograph() {
            lifecycleScope.launch {
                ptLoadingDialog.show()
                delay(500)
                takePicture()
            }
        }

        fun toCopy() {
            CopyUtils.copyClicks(this@CameraActivity, binding.edCameraDown.text.toString())
        }
    }

    /**
     * ???????????????
     */
    private fun updateLanguageItem() {
        binding.inCameraTitleSt.tvLanguageLeft.text =
            Locale(MlKitData.getInstance().sourceLang.value?.code).displayLanguage
        binding.inCameraTitleSt.tvLanguageRight.text =
            Locale(MlKitData.getInstance().targetLang.value?.code).displayLanguage
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, Constant.SELECT_PICTURE_RETURN)
    }

    private fun initCameraX() {
        imageCapture = ImageCapture.Builder()
            .build()
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            cameraProvider?.let { bindPreview(it) }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        cameraProvider.unbindAll()
        val preview: Preview = Preview.Builder()
            .build()
        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()
        preview.setSurfaceProvider(binding.pvCameraX.surfaceProvider)
        var camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
    }

    private var filepath: String? = null

    private fun takePicture() {
        if (filepath == null) {
            filepath = this.getExternalFilesDir(null)?.absolutePath + File.separator +
                    "camerax_image"
        }
        val imagePath =
            filepath + File.separator + "img_${Calendar.getInstance().timeInMillis}.jpeg"
        val imageFile = File(imagePath)
        if (!imageFile.parentFile.exists()) {
            imageFile.parentFile.mkdirs()
        }
        imageFile.createNewFile()
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(File(imagePath)).build()
        imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(error: ImageCaptureException) {
                    ToastUtils.toast(getString(R.string.photo_taking_failed))
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val imagePath = outputFileResults.savedUri
                    runOnUiThread {
                        imagePath?.let { imageRecognition(it) }
                    }
                }
            })
    }

    /**
     * ????????????
     */
    private fun imageRecognition(imagePath: Uri) {
        val image: InputImage =
            InputImage.fromFilePath(this@CameraActivity, imagePath)
        val recognizer = when (MlKitData.getInstance().sourceLang.value?.code) {
            TranslateLanguage.CHINESE -> {
                TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
            }
            TranslateLanguage.JAPANESE -> {
                TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
            }
            TranslateLanguage.KOREAN -> {
                TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            }
            else -> {
                TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            }
        }
        val result = recognizer.process(image)
            .addOnSuccessListener { visionText ->
                if (visionText.text.isEmpty()) {
                    ToastUtils.toast(getString(R.string.text_not_recognized))
                    ptLoadingDialog.dismiss()
                    return@addOnSuccessListener
                }
                binding.edCameraTop.setText("")
                binding.edCameraDown.setText("")
                binding.edCameraTop.setText(visionText.text)
                viewModel.translateRecognizedText(visionText.text)
                setPictureData(imagePath)
            }
            .addOnFailureListener { e ->
                identificationFailurePopUp()
            }
    }

    /**
     * ??????????????????
     */
    private fun identificationFailurePopUp() {
        val dialog: AlertDialog? = AlertDialog.Builder(this)
            .setTitle(getString(R.string.recognition_failed))
            .setMessage(getString(R.string.do_you_want_to_try_again))
            //????????????????????????
            .setNegativeButton("cancle") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setPositiveButton("try again") { dialog, _ ->
                dialog.dismiss()
            }.create()
        dialog?.show()
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.BLACK)
        dialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
    }

    /**
     * ????????????
     */
    fun setPictureData(imagePath: Uri) {
        Glide.with(this@CameraActivity).load(imagePath)
            .apply(
                RequestOptions.bitmapTransform(
                    MlKitData.GlideBlurTransformation(
                        this@CameraActivity
                    )
                )
            )
            .into(binding.imgShot)
        binding.conShot.visibility = View.VISIBLE
        binding.conCamera.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.SELECT_PICTURE_RETURN -> {
                KLog.e("TAG","onActivityResult------")
                App.whetherBackgroundSt = false
                if (resultCode == RESULT_OK) {
                    try {
                        val selectedImage = data?.data
                        imageRecognition(selectedImage as Uri)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            Constant.JUMP_LANGUAGE_PAGE -> {
                updateLanguageItem()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish()
        }
        return true
    }
}