package com.example.androidel

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.androidel.databinding.ActivitySendBinding


class SendActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySendBinding.inflate(layoutInflater) }
    private lateinit var sendAdapter: SendAdapter
    private lateinit var list: ArrayList<Uri>

    val OPEN_GALLERY = 1002
    private val VIDEO_FILE_REQUEST = 1003
    var btnSelect = 0

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initImageViewProfile()

        binding.btnExercise.isSelected = true

        binding.btnExercise.setOnClickListener {
            binding.btnExercise.isSelected = true
            binding.btnFood.isSelected = false
            binding.foodLayout.visibility = View.INVISIBLE
            binding.exerciseLayout.visibility = View.VISIBLE
        }

        binding.btnFood.setOnClickListener {
            binding.btnExercise.isSelected = false
            binding.btnFood.isSelected = true
            binding.foodLayout.visibility = View.VISIBLE
            binding.exerciseLayout.visibility = View.INVISIBLE
        }

        sendAdapter = SendAdapter(
            onClick = {
                navigateVideo()
            }
        )

        binding.recyclerView.adapter = sendAdapter
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        binding.btnFood1.setOnClickListener {
            btnSelect = 1
            navigateGallery()
        }

        binding.btnFood2.setOnClickListener {
            btnSelect = 2
            navigateGallery()
        }

        binding.btnFood3.setOnClickListener {
            btnSelect = 3
            navigateGallery()
        }


        binding.edtOpinion.addTextChangedListener(object: TextWatcher {
            var maxText = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                maxText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(binding.edtOpinion.lineCount > 2) {
                    Toast.makeText(applicationContext, "최대 2줄입니다.", Toast.LENGTH_SHORT).show()
                    binding.edtOpinion.setText(maxText)
                    binding.edtOpinion.setSelection(binding.edtOpinion.length())
                }
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == OPEN_GALLERY)
        {
            if(resultCode == RESULT_OK)
            {
                var currentImageUri = data?.data
                try{
                    currentImageUri?.let {
                        val source = ImageDecoder.createSource(this.contentResolver, currentImageUri)
                        val bitmap = ImageDecoder.decodeBitmap(source)
                        when (btnSelect) {
                            1 -> binding.btnFood1?.setImageBitmap(Bitmap.createScaledBitmap(bitmap, binding.btnFood1.width-2, binding.btnFood1.height-2, false))
                            2 -> binding.btnFood2?.setImageBitmap(Bitmap.createScaledBitmap(bitmap, binding.btnFood1.width-2, binding.btnFood1.height-2, false))
                            3 -> binding.btnFood3?.setImageBitmap(Bitmap.createScaledBitmap(bitmap, binding.btnFood1.width-2, binding.btnFood1.height-2, false))
                        }
                    }
                }catch(e: Exception)
                {
                    e.printStackTrace()
                }
            }
            else if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "사진 선택 취소", Toast.LENGTH_LONG).show();
            }
        }

        if(requestCode == VIDEO_FILE_REQUEST) {
            if(resultCode == RESULT_OK)
            {
                list = arrayListOf()
                val videoUri = data?.clipData
                if (videoUri == null) {
                    sendAdapter.setVideoOne(data?.data)
                }

                var count = data?.clipData?.itemCount
                if(count == null) {
                    count = 1
                }
//                if (count < exerciseKind.size) {
//                    Toast.makeText(applicationContext, "순서대로 총 영상 ${exerciseKind.size}개 선택해 주세요", Toast.LENGTH_LONG).show()
//                }
//                if (count >= exerciseKind.size) {
                try{
                    videoUri?.let {
                        for (i in 0 until count) {
                            val imageUri = videoUri.getItemAt(i).uri
                            list.add(imageUri)
                        }
                        sendAdapter.setVideoList(list)
                    }
                }catch(e: Exception)
                {
                    e.printStackTrace()
                }
            }

            else if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(this, "영상 선택 취소", Toast.LENGTH_LONG).show();
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            OPEN_GALLERY -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(this, "권한을 승인하셨습니다.", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "권한을 거부하셨습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun initImageViewProfile() {
        when {
            // 갤러리 접근 권한이 있는 경우
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
            }

            // 갤러리 접근 권한이 없는 경우 & 교육용 팝업을 보여줘야 하는 경우
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showPermissionContextPopup()
            }

            // 권한 요청 하기(requestPermissions) -> 갤러리 접근(onRequestPermissionResult)
            else ->
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    OPEN_GALLERY
                )
        }
    }

    private fun navigateVideo() {
        val vod_intent = Intent()
        //ACTION_GET_CONTENT을 하게되면, 갤러리뿐 아니라 다른 저장공간에도 접근한다.
//                      vod_intent.setAction(Intent.ACTION_GET_CONTENT);
        vod_intent.type = "video/*"
        vod_intent.action = Intent.ACTION_GET_CONTENT
        vod_intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(vod_intent, VIDEO_FILE_REQUEST)
    }

    fun navigateGallery() {
        val intent = Intent()
        // 가져올 컨텐츠들 중에서 Image 만을 가져온다.
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        // 갤러리에서 이미지를 선택한 후, 프로필 이미지뷰를 수정하기 위해 갤러리에서 수행한 값을 받아오는 startActivityForeResult를 사용한다.
        startActivityForResult(intent, OPEN_GALLERY)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showPermissionContextPopup() {
        AlertDialog.Builder(this)
            .setTitle("권한이 필요합니다.")
            .setMessage("프로필 이미지를 바꾸기 위해서는 갤러리 접근 권한이 필요합니다.")
            .setPositiveButton("동의하기") { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), OPEN_GALLERY)
            }
            .setNegativeButton("취소하기") { _, _ -> }
            .create()
            .show()
    }

    fun createThumbnail(activity: Context?, path: String?): Bitmap? {
        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        var bitmap: Bitmap? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(activity, Uri.parse(path))
            //timeUs는 마이크로 초 이므로 1000000초 곱해줘야 초단위다.
            bitmap = mediaMetadataRetriever.getFrameAtTime(
                1000000,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            mediaMetadataRetriever?.release()
        }
        return bitmap
    }
}