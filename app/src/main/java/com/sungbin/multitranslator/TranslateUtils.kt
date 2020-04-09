package com.sungbin.multitranslator

import android.os.StrictMode
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.net.URLEncoder


@Suppress("NonAsciiCharacters")
object TranslateUtils {

    init {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    fun papago(source: String, target: String, string: String): String {
        return try {
            val text = string.replace("\n", "§").replace("\\n", "§")
            var cash = ""
            var cash2 = ""
            val sourceCode = try {
                PapagoLanguage.valueOf(source).value
            }
            catch (e: Exception){
                cash = "[파파고에서 지원하지 않는 언어입니다]\n입력 언어를 한국어로 설정합니다.\n\n"
                PapagoLanguage.valueOf("한국어").value
            }
            val targetCode = try {
                PapagoLanguage.valueOf(target).value
            }
            catch (e: Exception){
                cash2 = "[파파고에서 지원하지 않는 언어입니다]\n번역 언어를 영어로 설정합니다.\n\n"
                PapagoLanguage.valueOf("영어").value
            }
            val query = URLEncoder.encode(text, "UTF-8")
            val apiURL = "https://openapi.naver.com/v1/papago/n2mt"
            val url = URL(apiURL)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "POST"
            con.setRequestProperty("X-Naver-Client-Id", PAPAGO_ID)
            con.setRequestProperty("X-Naver-Client-Secret", PAPAGO_SECRET)
            val postParams = "source=$sourceCode&target=$targetCode&text=$query"
            con.doOutput = true
            val wr = DataOutputStream(con.outputStream)
            wr.writeBytes(postParams)
            wr.flush()
            wr.close()
            val responseCode = con.responseCode
            val br: BufferedReader
            br = if (responseCode == 200) {
                BufferedReader(InputStreamReader(con.inputStream))
            } else {
                BufferedReader(InputStreamReader(con.errorStream))
            }
            var inputLine: String?
            val response = StringBuffer()
            while (br.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            br.close()
            val result = response.toString()
            if (result.contains("translatedText")) {
                cash + cash2 + result.split("translatedText\":\"")[1].split("\"")[0]
                    .replace("§", "\n").trimIndent()
            } else {
                cash + cash2 + "번역중 오류 발생!\n\n" + result.split("errorMessage\":\"")[1]
                    .split("\",\"errorCode")[0]
            }
        } catch (e: Exception) {
            "번역중 오류 발생!\n\n" + e.message
        }
    }

   fun google(source: String, target: String, text: String): String {
        return try {
            val string = text.replace("\n", "§").replace("\\n", "§")
            val sourceCode = GoogleLanguage.valueOf(source).value
            val targetCode = GoogleLanguage.valueOf(target).value
            val value = URLEncoder.encode(string, "UTF-8")
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val url =
                URL("http://translate.googleapis.com/translate_a/single?client=gtx&sl=$sourceCode&tl=$targetCode&dt=t&q=$value&ie=UTF-8&oe=UTF-8")
            val con: URLConnection = url.openConnection()
            con.setRequestProperty("User-Agent", "Mozilla/5.0")
            val br =
                BufferedReader(InputStreamReader(con.getInputStream(), "UTF-8"))
            var inputLine: String?
            val response = StringBuffer()
            while (br.readLine().also { inputLine = it } != null) {
                response.append(inputLine)
            }
            br.close()
            response.toString().split("\"")[1].replace("§", "\n").trimIndent()
        } catch (e: Exception) {
            e.toString()
        }
    }

   fun daum(source:String, target: String, string: String): String {
       return try {
           val text = string.replace("\n", "§").replace("\\n", "§")
           val query = URLEncoder.encode(text, "UTF-8")
           var cash = ""
           var cash2 = ""
           val sourceCode = try {
               DaumLanguage.valueOf(source).value
           }
           catch (e: Exception){
               cash = "[카카오 번역에서 지원하지 않는 언어입니다]\n입력 언어를 한국어로 설정합니다.\n\n"
               DaumLanguage.valueOf("한국어").value
           }
           val targetCode = try {
               DaumLanguage.valueOf(target).value
           }
           catch (e: Exception){
               cash2 = "[카카오 번역에서 지원하지 않는 언어입니다]\n번역 언어를 영어로 설정합니다.\n\n"
               DaumLanguage.valueOf("영어").value
           }
           val apiURL =
               "https://kapi.kakao.com/v1/translation/translate?src_lang=$sourceCode&target_lang=$targetCode&query=$query"
           val url = URL(apiURL)
           val con = url.openConnection() as HttpURLConnection
           con.requestMethod = "GET"
           con.setRequestProperty("Authorization", "KakaoAK $KAKAO_KEY")
           con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
           con.setRequestProperty("charset", "utf-8")
           con.useCaches = false
           con.doInput = true
           con.doOutput = true
           val responseCode = con.responseCode
           val br: BufferedReader
           br = if (responseCode == 200) {
               BufferedReader(InputStreamReader(con.inputStream))
           } else {
               BufferedReader(InputStreamReader(con.errorStream))
           }
           var inputLine: String?
           val response = StringBuffer()
           while (br.readLine().also { inputLine = it } != null) {
               response.append(inputLine)
           }
           br.close()
           val result = response.toString()
           if (result.contains("[[\"")) {
               cash + cash2 + result.split("[[\"")[1].split("\"]]}")[0].replace("\"],[\"", "\n")
                   .replace("[]", "\n").replace("§", "\n").replace("\"],", "")
                   .replace(",[\"", "").trimIndent()
           } else {
               cash + cash2 + "번역중 오류 발생!\n\n$result"
           }
       } catch (e: Exception) {
           "번역중 오류 발생!\n\n" + e.message
       }
   }

    private enum class PapagoLanguage(var value: String) {
        한국어("ko"), 영어("en"), 중국어("zh-CN"), 스페인어("es"),
        프랑스어("fr"), 베트남어("vi"), 태국어("th"), 인도네시아어("id")
    }

    private enum class GoogleLanguage(var value: String) {
        갈리시아어("gl"), 구자라트어("gu"), 그리스어("el"), 네덜란드어("nl"),
        네팔어("ne"), 노르웨이어("no"), 덴마크어("da"), 독일어("de"), 라오어("lo"),
        라트비아어("lv"), 라틴어("la"), 러시아어("ru"), 루마니아어("ro"), 룩셈부르크어("lb"),
        리투아니아어("lt"), 마라티어("mr"), 마오리어("mi"), 마케도니아어("mk"), 말라가시어("mg"),
        말라얄람어("ml"), 말레이어("ms"), 몰타어("mt"), 몽골어("mn"), 몽어("hmn"), 미얀마어("my"),
        바스크어("eu"), 베트남어("vi"), 벨라루스어("be"), 벵골어("bn"), 보스니아어("bs"), 불가리아어("bg"),
        사모아어("sm"), 세르비아어("sr"), 세부아노("ceb"), 세소토어("st"), 소말리아어("so"), 쇼나어("sn"),
        순다어("su"), 스와힐리어("sw"), 스웨덴어("sv"), 스코틀랜드게일어("gd"), 스페인어("es"),
        슬로바키아어("sk"), 슬로베니아어("sl"), 신디어("sd"), 신할라어("si"), 아랍어("ar"),
        아르메니아어("hy"), 아이슬란드어("is"), 아이티크리올어("ht"), 아일랜드어("ga"), 아제르바이잔어("az"),
        아프리칸스어("af"), 알바니아어("sq"), 암하라어("am"), 에스토니아어("et"), 에스페란토어("eo"),
        영어("en"), 요루바어("yo"), 우르두어("ur"), 우즈베크어("uz"), 우크라이나어("uk"), 웨일즈어("cy"),
        이그보어("ig"), 이디시어("yi"), 이탈리아어("it"), 인도네시아어("id"), 일본어("ja"), 자바어("jw"),
        조지아어("ka"), 줄루어("zu"), 중국어("zh-CN"), 체와어("ny"), 체코어("cs"), 카자흐어("kk"),
        카탈로니아어("ca"), 칸나다어("kn"), 코르시카어("co"), 코사어("xh"), 쿠르드어("ku"),
        크로아티아어("hr"), 크메르어("km"), 키르기스어("ky"), 타갈로그어("tl"), 타밀어("ta"),
        타지크어("tg"), 태국어("th"), 터키어("tr"), 텔루구어("te"), 파슈토어("ps"), 펀자브어("pa"),
        페르시아어("fa"), 포르투갈어("pt"), 폴란드어("pl"), 프랑스어("fr"), 프리지아어("fy"),
        핀란드어("fi"), 하와이어("haw"), 하우사어("ha"), 한국어("ko"), 헝가리어("hu"), 히브리어("iw"), 힌디어("hi")
    }

    private enum class DaumLanguage(var value: String) {
        한국어("kr"), 영어("en"), 일본어("jp"), 중국어("cn"), 베트남어("vi"),
        인도네시아어("id"), 아랍어("ar"), 벵골어("bn"), 독일어("de"), 스페인어("es"),
        프랑스어("fr"), 힌디어("hi"), 이탈리아어("it"), 말레이어("ms"), 네덜란드어("nl"),
        포르투갈어("pt"), 러시아어("ru"), 태국어("th"), 터키어("tr")
    }

}
