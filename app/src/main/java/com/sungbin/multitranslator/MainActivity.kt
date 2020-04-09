package com.sungbin.multitranslator

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.*
import android.text.SpannableStringBuilder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import cn.pedant.SweetAlert.SweetAlertDialog
import com.leo.searchablespinner.SearchableSpinner
import com.leo.searchablespinner.interfaces.OnItemSelectListener
import com.marcoscg.licenser.Library
import com.marcoscg.licenser.License
import com.marcoscg.licenser.LicenserDialog
import com.ogaclejapan.smarttablayout.SmartTabLayout.TabProvider
import com.sungbin.sungbintool.DataUtils
import com.sungbin.sungbintool.ToastUtils
import com.sungbin.sungbintool.Utils
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        view_pager.adapter = ViewPagerAdapter(this, ResultItems("번역버튼을 눌러주세요.", "번역버튼을 눌러주세요.", "번역버튼을 눌러주세요."))
        tab.setCustomTabView(TabProvider { container, position, adapter ->
            val view: View =  LayoutInflater.from(this).inflate(R.layout.custom_tab_icon_and_text, container, false)
            val text = view.findViewById<TextView>(R.id.tab_text)
            text.text = adapter.getPageTitle(position)
            val icon = view.findViewById<ImageView>(R.id.tab_icon)
            when(position){
                0 -> {
                    val params = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT)
                    params.setMargins(40, 5, 0, 0)
                    params.gravity = Gravity.CENTER_VERTICAL
                    text.layoutParams = params
                    icon.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_naver_white_24dp))
                }
                1 -> icon.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_google_white_24dp))
                else -> icon.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.ic_daum_white_24dp))
            }
            return@TabProvider view
        })
        tab.setViewPager(view_pager)

        iv_setting.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_setting_layout, null, false)
            val layout = view.findViewById<ScrollView>(R.id.main_layout)
            val btnOpensource = layout.findViewById<Button>(R.id.btn_opensource)
            dialog.setView(layout)

            val alert = dialog.create()
            alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alert.window!!.setWindowAnimations(R.style.DialogAnimation)
            alert.show()

            btnOpensource.setOnClickListener {
                showOpenSourceLicense()
            }
        }
        iv_mic.setOnClickListener {
            ToastUtils.show(applicationContext, getString(R.string.process_make),
                ToastUtils.SHORT, ToastUtils.INFO)
        }
        iv_copy.setOnClickListener {
            ToastUtils.show(applicationContext, getString(R.string.need_translate_first),
                ToastUtils.SHORT, ToastUtils.INFO)
        }
        iv_tts.setOnClickListener {
            ToastUtils.show(applicationContext, getString(R.string.process_make),
                ToastUtils.SHORT, ToastUtils.INFO)
        }
        iv_clear_text.setOnClickListener {
            et_input.text = SpannableStringBuilder("")
        }
        iv_translate.setOnClickListener {
            val inputLanguageData = DataUtils.readData(applicationContext, "input", "null")
            val translateLanguageData = DataUtils.readData(applicationContext, "translate", "null")

            if(inputLanguageData == "null"){
                ToastUtils.show(applicationContext, getString(R.string.need_language_setting_first),
                    ToastUtils.SHORT, ToastUtils.ERROR)
            }
            else {
                val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
                pDialog.progressHelper.barColor =
                    ContextCompat.getColor(applicationContext, R.color.colorPrimary)
                pDialog.titleText = getString(R.string.process_translate)
                pDialog.setCancelable(false)
                pDialog.show()
                TranslateTask(
                    this, et_input.text.toString(),
                    inputLanguageData, translateLanguageData, view_pager, pDialog,
                    iv_tts, iv_copy
                ).execute()
            }
        }

        iv_language.setOnClickListener {
            val inputLanguageData = DataUtils.readData(applicationContext, "input", "null")
            val translateLanguageData = DataUtils.readData(applicationContext, "translate", "null")

            val dialog = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.dialog_language_layout, null, false)
            val layout = view.findViewById<ScrollView>(R.id.main_layout)
            val btnChooseInputLanguage = layout.findViewById<Button>(R.id.btn_choose_input_language)
            val btnChooseTranslateLanguage = layout.findViewById<Button>(R.id.btn_choose_translate_language)
            val btnChooseDone = layout.findViewById<Button>(R.id.btn_choose_done)
            dialog.setView(layout)

            if(inputLanguageData != "null"){
                btnChooseInputLanguage.text = inputLanguageData
                btnChooseTranslateLanguage.text = translateLanguageData
            }

            val alert = dialog.create()
            alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alert.window!!.setWindowAnimations(R.style.DialogAnimation)
            alert.show()

            btnChooseInputLanguage.setOnClickListener {
                showChooseLanguageDialog(this, btnChooseInputLanguage)
            }
            btnChooseTranslateLanguage.setOnClickListener {
                showChooseLanguageDialog(this, btnChooseTranslateLanguage)
            }
            btnChooseDone.setOnClickListener {
                alert.cancel()
                if(btnChooseInputLanguage.text.toString() != "언어 선택" &&
                        btnChooseTranslateLanguage.text.toString() != "언어 선택") {
                    DataUtils.saveData(
                        applicationContext, "input",
                        btnChooseInputLanguage.text.toString()
                    )
                    DataUtils.saveData(
                        applicationContext, "translate",
                        btnChooseTranslateLanguage.text.toString()
                    )
                }
                else {
                    ToastUtils.show(applicationContext, "언어를 모두 선택해 주세요!",
                        ToastUtils.SHORT, ToastUtils.WARNING)
                }
            }
        }
    }

    private fun showChooseLanguageDialog(act: Activity, button: Button){
        val searchableSpinner = SearchableSpinner(act)
        searchableSpinner.windowTitle = "언어 선택"
        searchableSpinner.onItemSelectListener = object : OnItemSelectListener {
            override fun setOnItemSelectListener(position: Int, selectedString: String) {
                button.text = selectedString
            }
        }

        val languageList = arrayListOf(
            "갈리시아어", "구자라트어", "그리스어", "네덜란드어", "네팔어", "노르웨이어", "덴마크어", "독일어",
            "라오어", "라트비아어", "라틴어", "러시아어", "루마니아어", "룩셈부르크어", "리투아니아어", "마라티어",
            "마오리어", "마케도니아어", "말라가시어", "말라얄람어", "말레이어", "몰타어", "몽골어", "몽어", "미얀마어",
            "바스크어", "베트남어", "벨라루스어", "벵골어", "보스니아어", "불가리아어", "사모아어", "세르비아어",
            "세부아노어", "세소토어", "소말리아어", "쇼나어", "순다어", "스와힐리어", "스웨덴어", "스코틀랜드게일어",
            "스페인어", "슬로바키아어", "슬로베니아어", "신디어", "신할라어", "아랍어", "아르메니아어", "아이슬란드어",
            "아이티크리올어", "아일랜드어", "아제르바이잔어", "아프리칸스어", "알바니아어", "암하라어", "에스토니아어",
            "에스페란토어", "영어", "요루바어", "우르두어", "우즈베크어", "우크라이나어", "웨일즈어", "이그보어",
            "이디시어", "이탈리아어", "인도네시아어", "일본어", "자바어", "조지아어", "줄루어", "중국어", "체와어",
            "체코어", "카자흐어", "카탈로니아어", "칸나다어", "코르시카어", "코사어", "쿠르드어", "크로아티아어",
            "크메르어", "키르기스어", "타갈로그어", "타밀어", "타지크어", "태국어", "터키어", "텔루구어", "파슈토어",
            "펀자브어", "페르시아어", "포르투갈어", "폴란드어", "프랑스어", "프리지아어", "핀란드어", "하와이어",
            "하우사어", "한국어", "헝가리어", "히브리어", "힌디어"
        )

        searchableSpinner.setSpinnerListItems(languageList)
        searchableSpinner.show()
    }

    private fun showOpenSourceLicense(){
        LicenserDialog(this)
            .setTitle(getString(R.string.opensource_license))
            .setCustomNoticeTitle(getString(R.string.librarise_license))
            .setLibrary(
                Library(
                    "Android Support Libraries",
                    "https://developer.android.com/topic/libraries/support-library/index.html",
                    License.APACHE2
                )
            )
            .setLibrary(
                Library(
                    "SmartTabLayout",
                    "https://github.com/ogaclejapan/SmartTabLayout",
                    License.APACHE2
                )
            )
            .setLibrary(
                Library(
                    "Sweet Alert Dialog",
                    "https://github.com/pedant/sweet-alert-dialog",
                    License.MIT
                )
            )
            .setLibrary(
                Library(
                    "Searchable Spinner",
                    "https://github.com/leoncydsilva/SearchableSpinner",
                    License.MIT
                )
            )
            .setLibrary(
                Library(
                    "AndroidUtils",
                    "https://github.com/sungbin5304/AndroidUtils",
                    License.APACHE2
                )
            )
            .setLibrary(
                Library(
                    "Licenser",
                    "https://github.com/marcoscgdev/Licenser",
                    License.MIT
                )
            )
            .setPositiveButton(
                getString(R.string.string_cancel)
            ) { _, _ ->
            }
            .show()
    }

    internal open class TranslateTask constructor(
        activity: Activity, string: String, sourceLanguage: String,
        targetLanguage: String, viewPager: ViewPager, dialog: SweetAlertDialog,
        tts: ImageView, copy: ImageView) :
        AsyncTask<Void?, Void?, Void?>() {

        private var act = activity
        private var pDialog = dialog
        private var text = string
        private var source = sourceLanguage
        private var target = targetLanguage
        private var view = viewPager
        private var ttsBtn = tts
        private var copyBtn = copy

        @SuppressLint("InflateParams")
        private fun showCopyDialog(papago: String, google: String, daum: String){
            val dialog = AlertDialog.Builder(act)
            val view = LayoutInflater.from(act).inflate(R.layout.dialog_copy_layout, null, false)
            val layout = view.findViewById<ScrollView>(R.id.main_layout)
            val btnCopyPapago = layout.findViewById<Button>(R.id.btn_copy_papago)
            val btnCopyGoogle = layout.findViewById<Button>(R.id.btn_copy_google)
            val btnCopyDaum = layout.findViewById<Button>(R.id.btn_copy_daum)
            dialog.setView(layout)

            val alert = dialog.create()
            alert.window!!.setBackgroundDrawableResource(android.R.color.transparent)
            alert.window!!.setWindowAnimations(R.style.DialogAnimation)
            alert.show()

            btnCopyPapago.setOnClickListener {
                Utils.copy(act, papago)
                alert.cancel()
            }
            btnCopyGoogle.setOnClickListener {
                Utils.copy(act, google)
                alert.cancel()
            }
            btnCopyDaum.setOnClickListener {
                Utils.copy(act, daum)
                alert.cancel()
            }
        }

        override fun doInBackground(vararg params: Void?): Void? {
            return try {
                val handler: Handler = @SuppressLint("HandlerLeak")
                object : Handler(Looper.getMainLooper()) {
                    override fun handleMessage(msg: Message) {
                        val papago = TranslateUtils.papago(source, target, text)
                        val google = TranslateUtils.google(source, target, text)
                        val daum = TranslateUtils.daum(source, target, text)
                        val item = ResultItems(papago, google, daum)
                        val newAdapter = ViewPagerAdapter(act, item)
                        view.adapter = newAdapter

                        copyBtn.setOnClickListener {
                            showCopyDialog(papago, google, daum)
                        }
                    }
                }
                val message = handler.obtainMessage()
                handler.sendMessage(message)
                null
            } catch (e: Exception) {
                pDialog.titleText = e.toString()
                null
            }
        }

        override fun onPostExecute(any: Void?) {
            pDialog.cancel()
        }
    }

}